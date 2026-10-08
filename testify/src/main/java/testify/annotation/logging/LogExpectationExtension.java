/*
 * Copyright 2026 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package testify.annotation.logging;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import testify.annotation.ExpectLog;
import testify.annotation.failure.ExpectFailureExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;

/**
 * Backs the {@link ExpectLog} annotation.
 *
 * <p>Before the test runs, attaches a capturing {@link Handler} to each named logger
 * referenced by the annotations on the test method.  After the test runs (whether it
 * passed or threw), asserts that the captured records satisfy every {@code @ExpectLog}
 * expectation, then removes the handler.
 *
 * <p>Assertions are made even when the test body threw — this lets
 * {@code @ExpectLog} compose with {@code assertThrows}.
 */
public final class LogExpectationExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Namespace NS = Namespace.create(LogExpectationExtension.class);
    private static final String HANDLER_KEY = "capturingHandler";
    private static final String LEVEL_KEY = "savedLevels";

    @Override
    public void beforeTestExecution(ExtensionContext ctx) {
        List<ExpectLog> annotations = findRepeatableAnnotations(ctx.getRequiredTestMethod(), ExpectLog.class);
        if (annotations.isEmpty()) return;

        CapturingHandler handler = new CapturingHandler();
        java.util.Map<String, Level> savedLevels = new java.util.LinkedHashMap<>();

        // Attach to every distinct logger named by the annotations.
        // The root logger ("") receives records from all loggers, so attaching
        // there is enough when the logger name is left at its default.
        annotations.stream()
                .map(ExpectLog::logger)
                .distinct()
                .forEach(name -> {
                    Logger logger = Logger.getLogger(name);
                    logger.addHandler(handler);
                    // Ensure the logger itself isn't filtering the records out; save original level for restore
                    if (logger.getLevel() == null || logger.getLevel().intValue() > Level.ALL.intValue()) {
                        savedLevels.put(name, logger.getLevel());
                        logger.setLevel(Level.ALL);
                    }
                });

        ctx.getStore(NS).put(HANDLER_KEY, handler);
        ctx.getStore(NS).put(LEVEL_KEY, savedLevels);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterTestExecution(ExtensionContext ctx) {
        CapturingHandler handler = ctx.getStore(NS).remove(HANDLER_KEY, CapturingHandler.class);
        if (handler == null) return;

        List<ExpectLog> annotations = findRepeatableAnnotations(ctx.getRequiredTestMethod(), ExpectLog.class);

        // Detach and restore log levels before asserting so that assertion failures don't add spurious records
        java.util.Map<String, Level> savedLevels = (java.util.Map<String, Level>)
                ctx.getStore(NS).remove(LEVEL_KEY, java.util.Map.class);
        annotations.stream()
                .map(ExpectLog::logger)
                .distinct()
                .forEach(name -> {
                    Logger logger = Logger.getLogger(name);
                    logger.removeHandler(handler);
                    if (savedLevels != null && savedLevels.containsKey(name)) {
                        logger.setLevel(savedLevels.get(name));
                    }
                });

        List<LogRecord> records = handler.records;

        for (ExpectLog expectation : annotations) {
            Pattern pattern = Pattern.compile(expectation.value());
            ExpectLog.LogLevel expectedLevel = expectation.level();

            boolean matched = records.stream().anyMatch(record -> {
                if (expectedLevel != ExpectLog.LogLevel.ANY && !record.getLevel().equals(expectedLevel.level))
                    return false;
                String message = record.getMessage();
                return message != null && pattern.matcher(message).find();
            });

            if (!matched) {
                String description = expectedLevel == ExpectLog.LogLevel.ANY
                        ? "matching /" + expectation.value() + "/"
                        : "at level " + expectedLevel + " matching /" + expectation.value() + "/";
                AssertionError failure = new AssertionError("Expected a log record " + description
                        + " but none was found among " + records.size() + " captured record(s)."
                        + (records.isEmpty() ? "" : " Messages were:\n" + formatRecords(records)));

                // If @ExpectFailure is in play, deposit the error into the shared store
                // so ExpectFailureExtension can consume and validate it rather than letting
                // it surface as an unhandled test failure.
                if (ExpectFailureExtension.isSuppressing(ctx)) {
                    ExpectFailureExtension.depositFailure(ctx, failure);
                    return; // stop checking further expectations — one failure is enough
                }
                throw failure;
            }
        }
    }

    private static String formatRecords(List<LogRecord> records) {
        StringBuilder sb = new StringBuilder();
        for (LogRecord r : records) {
            sb.append("  [").append(r.getLevel()).append("] ").append(r.getMessage()).append('\n');
        }
        return sb.toString();
    }

    private static final class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) { records.add(record); }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }
}
