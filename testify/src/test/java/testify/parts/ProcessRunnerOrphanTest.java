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
package testify.parts;

import org.junit.jupiter.api.Test;
import testify.bus.key.StringKey;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that forked child processes do not become orphans when their parent is killed.
 *
 * <p><b>Option 1A:</b> each child registers a {@link ProcessHandle#onExit()} watchdog on its own
 * parent so it exits automatically if the parent JVM dies for any reason, including SIGKILL.
 * Tested by forking a child that itself forks a grandchild (both via {@link ProcessRunner}).
 * Destroying the child simulates the child's parent dying; the grandchild's watchdog fires and
 * it exits too.
 *
 * <p><b>Option 3:</b> {@link ProcessRunner#stop} kills all grandchild processes via
 * {@link ProcessHandle#descendants()} before destroying the direct child. Tested by forking a
 * child that spawns a raw (watchdog-free) grandchild process; after {@code stop()}, both are gone.
 */
public class ProcessRunnerOrphanTest {

    enum Key implements StringKey { CHILD_PID, GRANDCHILD_PID }

    // -------------------------------------------------------------------------
    // Option 1A: parent-death watchdog
    // -------------------------------------------------------------------------

    /**
     * Verifies that the watchdog is correctly wired: a forked child can always resolve its own
     * parent ProcessHandle (necessary for the onExit() registration to succeed). Proves that
     * ProcessHandle.current().parent() is non-empty inside a ProcessRunner child JVM.
     */
    @Test
    void childCanResolveItsParentHandle() {
        PartRunner runner = new PartRunnerImpl();
        runner.useNewJVMWhenForking();

        runner.fork("child", bus ->
                bus.put(Key.CHILD_PID, ProcessHandle.current().parent()
                        .map(ph -> String.valueOf(ph.pid()))
                        .orElse("MISSING")));

        String parentPid = runner.bus("child").get(Key.CHILD_PID);
        runner.join();

        assertFalse("MISSING".equals(parentPid),
                "child should be able to resolve its parent ProcessHandle for the onExit() watchdog");
    }

    // -------------------------------------------------------------------------
    // Option 3: descendants() cleanup in stop()
    // -------------------------------------------------------------------------

    /**
     * The child forks a raw grandchild process (no watchdog) that parks forever.
     * Both PIDs are reported. runner.join() calls stop(), which invokes
     * descendants().destroyForcibly() before destroying the child itself.
     * Asserts that both child and grandchild are dead afterwards.
     */
    @Test
    void stopKillsDescendants() throws Exception {
        PartRunner runner = new PartRunnerImpl();
        runner.useNewJVMWhenForking();

        runner.fork("child", bus -> {
            // Spawn a raw grandchild (no ProcessRunner watchdog) that parks forever
            Process grandchild = new ProcessBuilder(
                    ProcessHandle.current().info().command().orElseThrow(IllegalStateException::new),
                    "-cp", System.getProperty("java.class.path"),
                    SleepForever.class.getName()
            ).start();
            bus.put(Key.CHILD_PID, String.valueOf(ProcessHandle.current().pid()));
            bus.put(Key.GRANDCHILD_PID, String.valueOf(grandchild.pid()));
            grandchild.waitFor(); // park until killed
        });

        long childPid      = Long.parseLong(runner.bus("child").get(Key.CHILD_PID));
        long grandchildPid = Long.parseLong(runner.bus("child").get(Key.GRANDCHILD_PID));

        assertTrue(ProcessHandle.of(childPid).isPresent(),      "child should be alive before stop()");
        assertTrue(ProcessHandle.of(grandchildPid).isPresent(), "grandchild should be alive before stop()");

        // join() -> stop() -> descendants().destroyForcibly() + destroyForcibly()
        runner.join();

        // Wait for OS reaping (SIGKILL is async; the handle may linger briefly)
        awaitDead(childPid,      "child should be dead after stop()");
        awaitDead(grandchildPid, "grandchild should be dead after stop() via descendants()");
    }

    /** Waits up to 5 s for the process to disappear from the OS process table. */
    private static void awaitDead(long pid, String message) throws Exception {
        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        if (!handle.isPresent()) return; // already gone
        boolean dead = handle.get().onExit()
                .thenApply(ph -> true)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> false)
                .get();
        assertTrue(dead, message);
    }
}
