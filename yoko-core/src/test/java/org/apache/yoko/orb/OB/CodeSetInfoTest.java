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

import java.util.stream.Stream;

import static org.apache.yoko.orb.OB.CodeSetInfo.shareCommonElement;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeSetInfoTest {
    static final short[]
            NO_SHORTS = {},
            JUST_ZERO = {0},
            ONE_SHORT = {1},
            EVEN_SHORTS = {-2,0,2,4,6,8,10,12},
            ODD_SHORTS = {-1,1,3,5,7,9,11},
            PRIME_SHORTS = {2,3,5,7,11,13},
            NEGATIVE_SHORTS = {-3,-2,-1},
            SQUARE_SHORTS = {0,1,4,9,16},
            CUBE_SHORTS = {0,1,8,27},
            PERFECT_SHORTS = {6,28};
    @Test void testCompareEmptyArrays() { assertFalse(shareCommonElement(NO_SHORTS, NO_SHORTS)); }
    @Test void testCompareOneEmptyArray() { assertFalse(shareCommonElement(NO_SHORTS, JUST_ZERO)); }
    @Test void testCompareOtherEmptyArray() { assertFalse(shareCommonElement(ONE_SHORT, NO_SHORTS)); }
    @Test void testNoMatches1() { assertFalse(shareCommonElement(EVEN_SHORTS, ODD_SHORTS)); }
    @Test void testNoMatches2() { assertFalse(shareCommonElement(PRIME_SHORTS, NEGATIVE_SHORTS)); }
    @Test void testNoMatches3() { assertFalse(shareCommonElement(ODD_SHORTS, PERFECT_SHORTS)); }
    @Test void testNoMatches4() { assertFalse(shareCommonElement(PERFECT_SHORTS, ODD_SHORTS)); }
    @Test void testMatchAtEnds1() { assertTrue(shareCommonElement(NEGATIVE_SHORTS, ODD_SHORTS)); }
    @Test void testMatchAtEnds2() { assertTrue(shareCommonElement(ODD_SHORTS, NEGATIVE_SHORTS)); }
    @Test void testMatchInMiddleOfLeft() { assertTrue(shareCommonElement(EVEN_SHORTS, PERFECT_SHORTS));}
    @Test void testMatchInMiddleOfRight() { assertTrue(shareCommonElement(PERFECT_SHORTS, EVEN_SHORTS));}
    @Test void testMatchSomewhere1() { assertTrue(shareCommonElement(EVEN_SHORTS, SQUARE_SHORTS));}
    @Test void testMatchSomewhere2() { assertTrue(shareCommonElement(EVEN_SHORTS, CUBE_SHORTS));}
    @Test void testMatchSomewhere3() { assertTrue(shareCommonElement(ODD_SHORTS, SQUARE_SHORTS));}
    @Test void testMatchSomewhere4() { assertTrue(shareCommonElement(NEGATIVE_SHORTS, EVEN_SHORTS));}
    @Test void testMatchSomewhere5() { assertTrue(shareCommonElement(NEGATIVE_SHORTS, ODD_SHORTS));}
}