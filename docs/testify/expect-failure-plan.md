# Plan: `@ExpectFailure` Annotation & Test Inlining

## 1. Goal
Introduce an `@ExpectFailure` annotation and supporting JUnit 5 extension in `testify` that inverts a test's outcome when an expected exception or `AssertionError` is thrown (from the test method body or lifecycle callbacks like [`LogExpectationExtension.afterTestExecution`](../../testify/src/main/java/testify/annotation/logging/LogExpectationExtension.java:78)).

This eliminates separate negative fixture classes (like `ExpectLogTest$FailingFixtures`) and the need for `EngineTestKit`, allowing negative tests to be written inline as standard `@Test` methods.

---

## 2. Architecture & Design

### A. The Annotation: `testify.annotation.ExpectFailure`
Location: [`testify/src/main/java/testify/annotation/ExpectFailure.java`](../../testify/src/main/java/testify/annotation/ExpectLog.java)

```java
package testify.annotation;

import org.junit.jupiter.api.extension.ExtendWith;
import testify.annotation.failure.ExpectFailureExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith(ExpectFailureExtension.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectFailure {
    /** Expected exception type (default: AssertionError.class). */
    Class<? extends Throwable> value() default AssertionError.class;

    /** Optional regex pattern to match against the exception message. */
    String regex() default ".*";
}
```

### B. The Extension: `testify.annotation.failure.ExpectFailureExtension`
Location: `testify/src/main/java/testify/annotation/failure/ExpectFailureExtension.java`

**Key Lifecycle Handling:**
- `@ExpectLog` assertions run during `afterTestExecution`.
- Implementing `InvocationInterceptor` (`interceptTestMethod`, `interceptTestTemplateMethod`) or coordinating `TestExecutionExceptionHandler` with `AfterTestExecutionCallback` allows catching failures from both the test method and chained extension callbacks.
- **When expected exception matches**: Suppress failure, test passes.
- **When unexpected exception or non-matching message**: Fail with descriptive mismatch error.
- **When test completes without throwing**: Fail with `AssertionError("Expected test to fail with <type>, but it passed.")`.

---

## 3. Implementation Tasks

1. **Create `@ExpectFailure` annotation**:
   - `testify/src/main/java/testify/annotation/ExpectFailure.java`
2. **Implement `ExpectFailureExtension`**:
   - `testify/src/main/java/testify/annotation/failure/ExpectFailureExtension.java`
3. **Refactor [`ExpectLogTest`](../../testify/src/test/java/testify/annotation/logging/ExpectLogTest.java)**:
   - Remove `FailingFixtures` static class and `EngineTestKit` boilerplate.
   - Inline the negative tests directly on `ExpectLogTest` using `@Test` + `@ExpectFailure` + `@ExpectLog`.
4. **Run Validation**:
   - Run `./gradlew :testify:test --rerun-tasks` and verify all tests pass with no stray classes discovered.
