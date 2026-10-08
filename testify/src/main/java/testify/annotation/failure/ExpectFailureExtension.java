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
package testify.annotation.failure;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import testify.annotation.ExpectFailure;

import java.util.regex.Pattern;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

/**
 * Backs the {@link ExpectFailure} annotation.
 *
 * <h2>Design</h2>
 * <p>JUnit 5's {@link TestExecutionExceptionHandler} is only invoked for exceptions thrown by
 * the test method itself, not for exceptions thrown by extension lifecycle callbacks such as
 * {@code AfterTestExecutionCallback}.  This means we cannot intercept a failure raised by
 * {@code LogExpectationExtension.afterTestExecution} from here via that interface alone.
 *
 * <p>The solution uses a two-phase coordination protocol:
 * <ol>
 *   <li>{@link BeforeTestExecutionCallback} — when {@code @ExpectFailure} is detected, sets
 *       the {@link #SUPPRESSING_KEY} flag in the extension store so that cooperating extensions
 *       (e.g. {@code LogExpectationExtension}) deposit their {@link AssertionError} into the
 *       store instead of throwing it directly.</li>
 *   <li>{@link TestExecutionExceptionHandler} — catches exceptions thrown directly by the
 *       test method body (not covered by the deposit protocol), stores them the same way.</li>
 *   <li>{@link AfterEachCallback} — after all lifecycle callbacks have run, validates that
 *       exactly one failure was deposited and that it matches the expected type + regex.
 *       If no failure was deposited the test is failed with "expected to fail but passed".</li>
 * </ol>
 *
 * <p>Cooperating extensions call {@link #isSuppressing} / {@link #depositFailure} to
 * participate in the protocol.
 */
public final class ExpectFailureExtension
        implements BeforeTestExecutionCallback, TestExecutionExceptionHandler, AfterEachCallback {

    private static final Namespace NS = Namespace.create(ExpectFailureExtension.class);

    /** Store key: set to {@code Boolean.TRUE} when suppression is active for this test. */
    public static final String SUPPRESSING_KEY = "suppressing";

    /** Store key: the deposited {@link Throwable} that is expected to be the test failure. */
    public static final String FAILURE_KEY = "depositedFailure";

    // ---- static helpers used by cooperating extensions ----

    /**
     * Returns {@code true} when {@code @ExpectFailure} is active for this test execution,
     * i.e. cooperating extensions should deposit failures rather than throw them.
     */
    public static boolean isSuppressing(ExtensionContext ctx) {
        return Boolean.TRUE.equals(ctx.getStore(NS).get(SUPPRESSING_KEY, Boolean.class));
    }

    /**
     * Deposits {@code failure} into the context store so {@link ExpectFailureExtension} can
     * validate it in {@link #afterEach}.  Only the first deposited failure is retained; subsequent
     * calls are no-ops (the first failure is the meaningful one).
     */
    public static void depositFailure(ExtensionContext ctx, Throwable failure) {
        ctx.getStore(NS).getOrComputeIfAbsent(FAILURE_KEY, k -> failure);
    }

    // ---- lifecycle callbacks ----

    @Override
    public void beforeTestExecution(ExtensionContext ctx) {
        if (isAnnotated(ctx)) {
            ctx.getStore(NS).put(SUPPRESSING_KEY, Boolean.TRUE);
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext ctx, Throwable thrown) throws Throwable {
        if (!isAnnotated(ctx)) throw thrown; // not our business — rethrow unchanged
        // Deposit the test-method exception so afterEach can validate it
        depositFailure(ctx, thrown);
        // Do NOT rethrow — we suppress it here
    }

    @Override
    public void afterEach(ExtensionContext ctx) {
        if (!isAnnotated(ctx)) return;

        ExpectFailure annotation = findAnnotation(ctx.getRequiredTestMethod(), ExpectFailure.class)
                .or(() -> findAnnotation(ctx.getRequiredTestClass(), ExpectFailure.class))
                .orElseThrow(); // shouldn't happen since isAnnotated() is true

        Throwable deposited = ctx.getStore(NS).get(FAILURE_KEY, Throwable.class);

        if (deposited == null) {
            throw new AssertionError(
                    "Expected test to fail with " + annotation.value().getName()
                            + " but it passed without throwing.");
        }

        Class<? extends Throwable> expectedType = annotation.value();
        if (!expectedType.isInstance(deposited)) {
            throw new AssertionError(
                    "Expected test to fail with " + expectedType.getName()
                            + " but it threw " + deposited.getClass().getName() + ": " + deposited.getMessage(),
                    deposited);
        }

        String regex = annotation.regex();
        String message = deposited.getMessage();
        if (!Pattern.compile(regex, Pattern.DOTALL).matcher(message == null ? "" : message).find()) {
            throw new AssertionError(
                    "Expected exception message to match /" + regex + "/ but was: " + message,
                    deposited);
        }
        // All checks passed — the expected failure was received; test passes.
    }

    // ---- helpers ----

    private static boolean isAnnotated(ExtensionContext ctx) {
        return findAnnotation(ctx.getRequiredTestMethod(), ExpectFailure.class).isPresent()
                || findAnnotation(ctx.getRequiredTestClass(), ExpectFailure.class).isPresent();
    }
}
