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
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.apache.yoko.orb.OB;

import org.apache.yoko.io.ReadBuffer;
import org.omg.CORBA.DATA_CONVERSION;

import static org.apache.yoko.util.MinorCodes.MinorUTF8Encoding;
import static org.apache.yoko.util.MinorCodes.MinorUTF8Overflow;
import static org.apache.yoko.util.MinorCodes.describeDataConversion;
import static org.omg.CORBA.CompletionStatus.COMPLETED_NO;

final class UTF8Reader extends CodeSetReader {
    public char read_char(ReadBuffer readBuffer) throws DATA_CONVERSION {
        byte first = readBuffer.readByte();

        // Direct mapping for characters <= 127
        if ((first & 0b1000_0000) == 0)
            return (char) first;

        char value;

        if ((first & 0b1110_0000) == 0b1100_0000) {
            // 5 free bits, i.e. 110.....
            value = (char) (first & 0b0001_1111);
        } else if ((first & 0b1111_0000) == 0b1110_0000) {
            // 4 free bits, i.e. 1110....
            value = (char) (first & 0b0000_1111);
            // read second byte
            if ((readBuffer.peekByte() & 0xc0) != 0x80) {
                throw new DATA_CONVERSION(describeDataConversion(MinorUTF8Encoding), MinorUTF8Encoding, COMPLETED_NO);
            }
            value <<= 6;
            value |= readBuffer.readByte() & 0b0011_1111;
        } else {
            // 16 bit overflow
            throw new DATA_CONVERSION(describeDataConversion(MinorUTF8Overflow), MinorUTF8Overflow, COMPLETED_NO);
        }
        if ((readBuffer.peekByte() & 0b1100_0000) != 0b1000_0000) {
            throw new DATA_CONVERSION(describeDataConversion(MinorUTF8Encoding), MinorUTF8Encoding, COMPLETED_NO);
        }

        value <<= 6;
        value |= readBuffer.readByte() & 0b0011_1111;

        return value;
    }

    public char read_wchar(ReadBuffer readBuffer, int len) throws DATA_CONVERSION {
        return read_char(readBuffer);
    }

    public int count_wchar(char first) {
        if ((first & 0x80) == 0)
            return 1;
        else if ((first & 0xf8) == 0xc0)
            return 2;
        else if ((first & 0xf8) == 0xe0)
            return 3;

        throw new DATA_CONVERSION(describeDataConversion(MinorUTF8Overflow), MinorUTF8Overflow, COMPLETED_NO);
    }

    public void set_flags(int flags) {
    }
}
