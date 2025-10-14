/*
 * Copyright 2025 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.apache.yoko.io;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.yoko.io.Buffer.createReadBuffer;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReadBufferTest {
    @Test
    void testRemainingBytes() {
        var rb = createReadBuffer("hello".getBytes(UTF_8));
        assertThat(new String(rb.copyRemainingBytes(), UTF_8), equalTo("hello"));
    }

    @Test
    void testRemainingBytesAfterRead() {
        var rb = createReadBuffer("ohello".getBytes(UTF_8));
        rb.readByte();
        assertThat(new String(rb.copyRemainingBytes(), UTF_8), equalTo("hello"));
    }

    @Test
    void testRemainingBytesAfterSkip() {
        var rb = createReadBuffer("well, hello".getBytes(UTF_8));
        rb.skipBytes(6);
        assertThat(new String(rb.copyRemainingBytes(), UTF_8), equalTo("hello"));
    }
}
