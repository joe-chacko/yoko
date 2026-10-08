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

import org.junit.jupiter.api.Test;
import testify.annotation.ExpectFailure;
import testify.annotation.ExpectLog;

import java.util.logging.Logger;

import static testify.annotation.ExpectLog.LogLevel.INFO;
import static testify.annotation.ExpectLog.LogLevel.WARNING;

public class ExpectLogTest {

    private static final Logger LOG = Logger.getLogger(ExpectLogTest.class.getName());

    // --- passing cases ---

    @Test
    @ExpectLog("hello world")
    void matchesMessageByRegex() {
        LOG.info("hello world");
    }

    @Test
    @ExpectLog("hel+o")
    void matchesMessageByRegexPattern() {
        LOG.info("hello");
    }

    @Test
    @ExpectLog(value = "hello", level = INFO)
    void matchesMessageAndLevel() {
        LOG.info("hello");
    }

    @Test
    @ExpectLog(value = "hello", logger = "testify.annotation.logging.ExpectLogTest")
    void matchesOnNamedLogger() {
        LOG.info("hello");
    }

    @Test
    @ExpectLog("first")
    @ExpectLog("second")
    void matchesMultipleExpectations() {
        LOG.info("first message");
        LOG.info("second message");
    }

    @Test
    @ExpectLog(value = "important", level = WARNING)
    void ignoresRecordsAtWrongLevel() {
        LOG.info("important but at INFO");   // should be ignored for the WARNING expectation
        LOG.warning("important at WARNING"); // this one satisfies it
    }

    // --- failing cases (expected to fail; @ExpectFailure inverts the outcome) ---

    @Test
    @ExpectFailure
    @ExpectLog("will-never-appear")
    void failsWhenNoRecordMatchesRegex() {
        LOG.info("something completely different");
    }

    @Test
    @ExpectFailure
    @ExpectLog(value = "hello", level = WARNING)
    void failsWhenRecordIsAtWrongLevel() {
        LOG.info("hello"); // INFO, but WARNING is required
    }

    @Test
    @ExpectFailure
    @ExpectLog("first")
    @ExpectLog("second-will-not-appear")
    void failsWhenOneOfMultipleExpectationsIsUnsatisfied() {
        LOG.info("first message");
        // "second-will-not-appear" is never logged
    }

    @Test
    @ExpectFailure(regex = ".*will-never-appear.*")
    @ExpectLog("will-never-appear")
    void failureMessageContainsExpectedPattern() {
        LOG.info("something completely different");
    }
}
