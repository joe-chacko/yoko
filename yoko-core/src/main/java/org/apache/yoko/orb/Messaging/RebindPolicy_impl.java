/*
 * Copyright 2024 IBM Corporation and others.
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
package org.apache.yoko.orb.Messaging;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.Messaging.REBIND_POLICY_TYPE;
import org.omg.Messaging.RebindPolicy;

final public class RebindPolicy_impl extends LocalObject
        implements RebindPolicy {
    private short value_;

    // ------------------------------------------------------------------
    // Standard IDL to Java Mapping
    // ------------------------------------------------------------------

    public short rebind_mode() {
        return value_;
    }

    public int policy_type() {
        return REBIND_POLICY_TYPE.value;
    }

    public Policy copy() {
        return this;
    }

    public void destroy() {
    }

    // ------------------------------------------------------------------
    // Yoko internal functions
    // Application programs must not use these functions directly
    // ------------------------------------------------------------------

    public RebindPolicy_impl(short value) {
        value_ = value;
    }
}
