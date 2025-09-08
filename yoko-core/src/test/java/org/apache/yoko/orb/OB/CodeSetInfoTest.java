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
package org.apache.yoko.orb.OB;

import org.junit.jupiter.api.Test;

import static org.apache.yoko.orb.OB.CodeSetInfo.shareCommonElement;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeSetInfoTest {
    static final int[]
            NO_INTS = {},
            JUST_ZERO = {0},
            ONE_INT = {1},
            EVEN_INTS = {-2,0,2,4,6,8,10,12},
            ODD_INTS = {-1,1,3,5,7,9,11},
            PRIME_INTS = {2,3,5,7,11,13},
            NEGATIVE_INTS = {-3,-2,-1},
            SQUARE_INTS = {0,1,4,9,16},
            CUBE_INTS = {0,1,8,27},
            PERFECT_INTS = {6,28};
    @Test void testCompareEmptyArrays() { assertFalse(shareCommonElement(NO_INTS, NO_INTS)); }
    @Test void testCompareOneEmptyArray() { assertFalse(shareCommonElement(NO_INTS, JUST_ZERO)); }
    @Test void testCompareOtherEmptyArray() { assertFalse(shareCommonElement(ONE_INT, NO_INTS)); }
    @Test void testNoMatches1() { assertFalse(shareCommonElement(EVEN_INTS, ODD_INTS)); }
    @Test void testNoMatches2() { assertFalse(shareCommonElement(PRIME_INTS, NEGATIVE_INTS)); }
    @Test void testNoMatches3() { assertFalse(shareCommonElement(ODD_INTS, PERFECT_INTS)); }
    @Test void testNoMatches4() { assertFalse(shareCommonElement(PERFECT_INTS, ODD_INTS)); }
    @Test void testMatchAtEnds1() { assertTrue(shareCommonElement(NEGATIVE_INTS, ODD_INTS)); }
    @Test void testMatchAtEnds2() { assertTrue(shareCommonElement(ODD_INTS, NEGATIVE_INTS)); }
    @Test void testMatchInMiddleOfLeft() { assertTrue(shareCommonElement(EVEN_INTS, PERFECT_INTS));}
    @Test void testMatchInMiddleOfRight() { assertTrue(shareCommonElement(PERFECT_INTS, EVEN_INTS));}
    @Test void testMatchSomewhere1() { assertTrue(shareCommonElement(EVEN_INTS, SQUARE_INTS));}
    @Test void testMatchSomewhere2() { assertTrue(shareCommonElement(EVEN_INTS, CUBE_INTS));}
    @Test void testMatchSomewhere3() { assertTrue(shareCommonElement(ODD_INTS, SQUARE_INTS));}
    @Test void testMatchSomewhere4() { assertTrue(shareCommonElement(NEGATIVE_INTS, EVEN_INTS));}
    @Test void testMatchSomewhere5() { assertTrue(shareCommonElement(NEGATIVE_INTS, ODD_INTS));}
}