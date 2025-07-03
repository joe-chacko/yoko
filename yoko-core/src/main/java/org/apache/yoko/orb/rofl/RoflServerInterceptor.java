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
package org.apache.yoko.orb.rofl;

import org.apache.yoko.util.rofl.RoflHelper;
import org.apache.yoko.util.rofl.RoflThreadLocal;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class RoflServerInterceptor extends LocalObject implements ServerRequestInterceptor {
    private static final String NAME = RoflServerInterceptor.class.getName();
    private final RoflHelper roflHelper;

    public RoflServerInterceptor(int slotId) {
        this.roflHelper = new RoflHelper(slotId);
    }
    public void receive_request_service_contexts(ServerRequestInfo ri) throws ForwardRequest {
        RoflThreadLocal.reset();
        roflHelper.findAndSave(ri);
    }

    public void receive_request(ServerRequestInfo ri) {}
    public void send_reply(ServerRequestInfo ri) { RoflThreadLocal.push(roflHelper.loadAndCreate(ri)); }
    public void send_exception(ServerRequestInfo ri) { RoflThreadLocal.push(roflHelper.loadAndCreate(ri)); }
    public void send_other(ServerRequestInfo ri) { RoflThreadLocal.push(roflHelper.loadAndCreate(ri)); }
    public String name() { return NAME; }
    public void destroy() {}
}
