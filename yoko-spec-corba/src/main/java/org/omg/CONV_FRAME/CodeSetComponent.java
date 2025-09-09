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
package org.omg.CONV_FRAME;

import org.omg.CORBA.portable.IDLEntity;

// IDL:omg.org/CONV_FRAME/CodeSetComponent:1.0
public final class CodeSetComponent implements IDLEntity {
    public int native_code_set;
    public int[] conversion_code_sets;

    public CodeSetComponent() {}

    public CodeSetComponent(int native_code_set, int... conversion_code_sets) {
        this.native_code_set = native_code_set;
        this.conversion_code_sets = conversion_code_sets;
    }
}
