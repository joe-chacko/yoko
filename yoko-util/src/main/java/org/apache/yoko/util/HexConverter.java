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
package org.apache.yoko.util;

import java.util.Arrays;

public final class HexConverter {
    private final static char[] NYBBLE_TO_HEX_CHAR = "0123456789abcdef".toCharArray();
    private final static byte[] HEX_CHAR_TO_NYBBLE = new byte['f' + 1];
    static {
        Arrays.fill(HEX_CHAR_TO_NYBBLE, (byte)-1);
        for (byte i = 0; i < NYBBLE_TO_HEX_CHAR.length; i++) {
            char c = NYBBLE_TO_HEX_CHAR[i];
            HEX_CHAR_TO_NYBBLE[c] = i;
            HEX_CHAR_TO_NYBBLE[Character.toUpperCase(c)] = i;
        }
    }

    private static char[] toHexChars(byte[] oct, int offset, int count) {
        assert offset + count <= oct.length;

        char[] result = new char[count * 2];

        for (int i = offset, j = 0; i < offset + count; i++) {
            result[j++] = NYBBLE_TO_HEX_CHAR[oct[i] >> 4 & 0x0f];
            result[j++] = NYBBLE_TO_HEX_CHAR[oct[i] >> 0 & 0x0f];
        }

        return result;
    }

    public static String toHex(byte[] oct) {
        return oct == null ? null : String.valueOf(toHexChars(oct, 0, oct.length));
    }

    public static String toHex(byte[] oct, int count) {
        return oct == null ? null : String.valueOf(toHexChars(oct, 0, count));
    }

    public static String toHex(byte[] oct, int offset, int count) {
        return oct == null ? null : String.valueOf(toHexChars(oct, offset, count));
    }

    public static byte[] fromHex(String str, int offset) {
        int slen = str.length() - offset;

        // Two ASCII characters for each octet
        if ((slen & 1) != 0) return null;

        byte[] oct = new byte[slen/2];

        try {

            for (int i = 0, j = offset; i < oct.length; i++) {
                char highChar = str.charAt(j++);
                char lowChar = str.charAt(j++);
                int high = HEX_CHAR_TO_NYBBLE[highChar];
                if (high < 0) return null;
                int low = HEX_CHAR_TO_NYBBLE[lowChar];
                if (low < 0) return null;


                oct[i] = (byte) ((high << 4) | low);
            }

            return oct;

        } catch (ArrayIndexOutOfBoundsException swallowed) {
            return null;
        }
    }

    public static byte[] fromHex(String str) {
        return fromHex(str, 0);
    }
}
