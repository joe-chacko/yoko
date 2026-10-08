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
package testify.annotation;

import org.junit.jupiter.api.extension.ExtendWith;
import testify.annotation.failure.ExpectFailureExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inverts a test's outcome: the test passes only when the test body (or a chained
 * extension callback such as {@code @ExpectLog}) throws the expected exception type
 * with a message matching the given regex.  If the test completes without throwing,
 * or throws an unexpected exception type / non-matching message, the test fails.
 */
@ExtendWith(ExpectFailureExtension.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectFailure {
    /** Expected exception type. Defaults to {@link AssertionError}. */
    Class<? extends Throwable> value() default AssertionError.class;

    /** Optional regex that the exception message must match. Defaults to {@code ".*"} (any). */
    String regex() default ".*";
}
