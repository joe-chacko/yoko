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
package org.apache.yoko.orb.IMR;

import static org.apache.yoko.util.MinorCodes.MinorReadIDMismatch;
import static org.apache.yoko.util.MinorCodes.MinorTypeMismatch;
import static org.apache.yoko.util.MinorCodes.describeBadOperation;
import static org.apache.yoko.util.MinorCodes.describeMarshal;
import static org.omg.CORBA.CompletionStatus.COMPLETED_NO;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

//
// IDL:orb.yoko.apache.org/IMR/NoSuchPOA:1.0
//
public final class _NoSuchPOAHelper
{
    public static void
    insert(Any any, _NoSuchPOA val)
    {
        OutputStream out = any.create_output_stream();
        write(out, val);
        any.read_value(out.create_input_stream(), type());
    }

    public static _NoSuchPOA
    extract(Any any)
    {
        if(any.type().equivalent(type()))
            return read(any.create_input_stream());
        else
            throw new BAD_OPERATION(
                describeBadOperation(MinorTypeMismatch),
                MinorTypeMismatch, COMPLETED_NO);
    }

    private static org.omg.CORBA.TypeCode typeCode_;

    public static org.omg.CORBA.TypeCode
    type()
    {
        if(typeCode_ == null)
        {
            ORB orb = ORB.init();
            StructMember[] members = new StructMember[1];

            members[0] = new StructMember();
            members[0].name = "poa";
            members[0].type = POANameHelper.type();

            typeCode_ = orb.create_exception_tc(id(), "NoSuchPOA", members);
        }

        return typeCode_;
    }

    public static String
    id()
    {
        return "IDL:orb.yoko.apache.org/IMR/NoSuchPOA:1.0";
    }

    public static _NoSuchPOA
    read(InputStream in)
    {
        if(!id().equals(in.read_string())) {
            throw new MARSHAL(
                describeMarshal(MinorReadIDMismatch),
                MinorReadIDMismatch,
                COMPLETED_NO);
        }

        _NoSuchPOA _ob_v = new _NoSuchPOA();
        _ob_v.poa = POANameHelper.read(in);
        return _ob_v;
    }

    public static void
    write(OutputStream out, _NoSuchPOA val)
    {
        out.write_string(id());
        POANameHelper.write(out, val.poa);
    }
}
