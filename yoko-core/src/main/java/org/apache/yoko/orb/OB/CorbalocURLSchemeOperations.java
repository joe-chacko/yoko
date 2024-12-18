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
package org.apache.yoko.orb.OB;

import org.apache.yoko.orb.OB.CorbalocURLSchemePackage.ProtocolAlreadyExists;

//
// IDL:orb.yoko.apache.org/OB/CorbalocURLScheme:1.0
//
/**
 *
 * CorbalocURLScheme implements the <code>corbaloc</code> URL scheme,
 * and serves as a registry for CorbalocProtocol objects.
 *
 * @see CorbalocProtocol
 *
 **/

public interface CorbalocURLSchemeOperations extends URLSchemeOperations
{
    //
    // IDL:orb.yoko.apache.org/OB/CorbalocURLScheme/add_protocol:1.0
    //
    /**
     *
     * Register a new <code>corbaloc</code> protocol.
     *
     * @param protocol The new protocol.
     *
     * @exception org.apache.yoko.orb.OB.CorbalocURLSchemePackage.ProtocolAlreadyExists Another protocol already exists
     * with the same name.
     *
     **/

    void
    add_protocol(CorbalocProtocol protocol)
        throws ProtocolAlreadyExists;

    //
    // IDL:orb.yoko.apache.org/OB/CorbalocURLScheme/find_protocol:1.0
    //
    /**
     *
     * Find a protocol with the given name.
     *
     * @param name The protocol name, in lower case.
     *
     * @return The CorbalocProtocol, or nil if no match was found.
     *
     **/

    CorbalocProtocol
    find_protocol(String name);
}
