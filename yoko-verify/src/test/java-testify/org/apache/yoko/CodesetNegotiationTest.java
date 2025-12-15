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
package org.apache.yoko;

import org.apache.yoko.orb.CORBA.InputStream;
import org.apache.yoko.orb.OB.CodeSetInfo;
import org.apache.yoko.orb.PortableServer.PolicyValue;
import org.hamcrest.collection.ArrayMatching;
import org.junit.jupiter.api.Test;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CORBA.ORB;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IOP.IOR;
import org.omg.IOP.IORHelper;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;
import testify.iiop.annotation.ConfigureOrb;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.apache.yoko.orb.PortableServer.PolicyValue.MULTIPLE_ID;
import static org.apache.yoko.orb.PortableServer.PolicyValue.NO_IMPLICIT_ACTIVATION;
import static org.apache.yoko.orb.PortableServer.PolicyValue.PERSISTENT;
import static org.apache.yoko.orb.PortableServer.PolicyValue.RETAIN;
import static org.apache.yoko.orb.PortableServer.PolicyValue.USER_ID;
import static org.apache.yoko.orb.PortableServer.PolicyValue.USE_DEFAULT_SERVANT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static testify.hex.HexParser.HEX_STRING;

/**
 * Test how the ORB behaves when negotiating codesets.
 */
@ConfigureOrb
public class CodesetNegotiationTest {

    interface Widget extends Remote { String name() throws RemoteException;}

    class WidgetImpl implements Widget {
        public String name() { return "Specific widget"; }
    }

    @Test
    void testComponentInGeneratedIor(ORB orb, POA root) throws Exception {
        org.omg.CORBA.Object ref = root.create_reference("IDL:Test:1.0");
        IOR ior = getIor(orb.object_to_string(ref));
        System.out.println(orb.object_to_string(ref));
        CodeSetComponentInfo[] arr = stream(ior.profiles)
                .filter(p -> p.tag == TAG_INTERNET_IOP.value)
                .flatMap(this::streamTaggedComponentsFromIor)
                .filter(tc -> tc.tag == TAG_CODE_SETS.value)
                .map(this::readCodeSetsFromComponent)
                .toArray(CodeSetComponentInfo[]::new);
        assertThat(arr, is(not(nullValue())));
        assertThat(arr.length, is(1));
        CodeSetComponentInfo info = arr[0];
        CodeSetComponent charCodeSetComponent = info.ForCharData;
        CodeSetComponent wcharCodeSetComponent = info.ForWcharData;
        assertThat(charCodeSetComponent.native_code_set, is(CodeSetInfo.UTF_8.id));
        assertThat(wcharCodeSetComponent.native_code_set, is(CodeSetInfo.UTF_16.id));
    }

    IOR getIor(String stringifiedIor) {
        // check this looks like an ior
        assertThat(stringifiedIor, startsWith("IOR:"));
        String hex = stringifiedIor.substring(4);
        // get the data from the hex representation
        byte[] bytes = HEX_STRING.parse(hex);
        // read it into an IOR
        InputStream in = new InputStream(bytes);
        assertThat("big-endian expected", in.read_boolean(), is(false));
        return IORHelper.read(in);
    }

    Stream<TaggedComponent> streamTaggedComponentsFromIor(TaggedProfile p) {
        InputStream in = new InputStream(p.profile_data);
        assertThat("big-endian expected", in.read_boolean(), is(false));
        ProfileBody_1_1 body = ProfileBody_1_1Helper.read(in);
        return stream(body.components);
    }

    CodeSetComponentInfo readCodeSetsFromComponent(TaggedComponent tc) {
        InputStream in = new InputStream(tc.component_data);
        assertThat("big-endian expected", in.read_boolean(), is(false));
        return CodeSetComponentInfoHelper.read(in);
    }

}

