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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.yoko.io.Buffer.createReadBuffer;
import static org.apache.yoko.io.Buffer.createWriteBuffer;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

class BufferTest {
    public static final String TEXT = "By these he was generally called by the euphonious name of 'Old Bramble-Buffer,'" +
            " although the title by which he was known among his own kith and kin was probably quite different," +
            " and has not been handed down to posterity.";
    private byte[] bytes;
    private byte[][] snippets;

    @BeforeEach
    void setup() {
        assertThat(TEXT.length(), greaterThan(200));
        bytes = TEXT.getBytes(UTF_8);
        assertThat(bytes.length, equalTo(TEXT.length()));
        snippets = new byte[][]{
                TEXT.substring(0, 50).getBytes(UTF_8),
                TEXT.substring(50, 100).getBytes(UTF_8),
                TEXT.substring(100, 150).getBytes(UTF_8),
                TEXT.substring(150, 200).getBytes(UTF_8),
                {},
                {},
                TEXT.substring(200).getBytes(UTF_8),
                {}
        };
    }

    @Test
    public void testReadFromStartWithNoData() {
        var wb = createWriteBuffer();
        assertThat(wb.readFromStart().available(), equalTo(0));
    }

    @Test
    public void testReadFromStartWithData() {
        var wb = createWriteBuffer();
        wb.ensureAvailable(bytes.length);
        wb.writeBytes(bytes);
        assertThat(wb.readFromStart().readByte(), equalTo((byte)'B'));
        assertThat(wb.readFromStart().available(), equalTo(bytes.length));
        // check the write buffer is unmodified
        assertBufferContains(wb, TEXT);
    }

    @Test
    public void testWriteSeveralByteArrays() {
        var wb = createWriteBuffer();
        int bytesWritten = 0;
        for (byte[] snippet: snippets) {
            wb.ensureAvailable(snippet.length);
            wb.writeBytes(snippet);
            bytesWritten += snippet.length;
            assertBufferContains(wb, TEXT.substring(0, bytesWritten));
        }
        assertBufferContains(wb, TEXT);
    }

    @Test
    public void testWriteByteArrayInParts() {
        var wb = createWriteBuffer();
        wb.ensureAvailable(bytes.length);
        wb.writeBytes(bytes, 0, 100);
        wb.writeBytes(bytes, 100, 100);
        wb.writeBytes(bytes, 200, bytes.length - 200);
        assertBufferContains(wb, TEXT);
    }

    @Test
    public void testReadBytes() {
        var wb = createWriteBuffer();
        int bytesWritten = 0;
        for (byte[] snippet: snippets) {
            wb.ensureAvailable(snippet.length);
            createReadBuffer(snippet).readBytes(wb);
            bytesWritten += snippet.length;
            assertBufferContains(wb, TEXT.substring(0, bytesWritten));
        }
        assertBufferContains(wb, TEXT);
    }

    @Test
    public void testReadBytesAfterSkipping() {
        var readBuffer = createReadBuffer(TEXT.getBytes(UTF_8));
        assertThat(readBuffer.available(), equalTo(TEXT.length()));
        readBuffer.skipBytes(100);
        System.out.println(readBuffer.dumpAllDataWithPosition());
        assertBufferContains(readBuffer, TEXT.substring(100));
    }

    private static void assertBufferContains(WriteBuffer wb, String expected) {
        assertBufferContains(wb.readFromStart(),                                                                        expected);
    }

    private static void assertBufferContains(ReadBuffer readBuffer, String expected) {
        final String actual = new String(readBuffer.copyRemainingBytes(), UTF_8);
        assertThat(actual, equalTo(expected));
    }
}
