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
package org.apache.yoko.orb.OAD;

import org.apache.yoko.orb.IMR.ProcessIDHelper;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

//
// IDL:orb.yoko.apache.org/OAD/ProcessEndpointManager:1.0
//
public abstract class ProcessEndpointManagerPOA
    extends Servant
    implements InvokeHandler,
               ProcessEndpointManagerOperations
{
    static final String[] _ob_ids_ =
    {
        "IDL:orb.yoko.apache.org/OAD/ProcessEndpointManager:1.0",
    };

    public ProcessEndpointManager
    _this()
    {
        return ProcessEndpointManagerHelper.narrow(super._this_object());
    }

    public ProcessEndpointManager
    _this(ORB orb)
    {
        return ProcessEndpointManagerHelper.narrow(super._this_object(orb));
    }

    public String[]
    _all_interfaces(POA poa, byte[] objectId)
    {
        return _ob_ids_;
    }

    public OutputStream
    _invoke(String opName,
            InputStream in,
            ResponseHandler handler)
    {
        final String[] _ob_names =
        {
            "establish_link"
        };

        int _ob_left = 0;
        int _ob_right = _ob_names.length;
        int _ob_index = -1;

        while(_ob_left < _ob_right)
        {
            int _ob_m = (_ob_left + _ob_right) / 2;
            int _ob_res = _ob_names[_ob_m].compareTo(opName);
            if(_ob_res == 0)
            {
                _ob_index = _ob_m;
                break;
            }
            else if(_ob_res > 0)
                _ob_right = _ob_m;
            else
                _ob_left = _ob_m + 1;
        }

        if(_ob_index == -1 && opName.charAt(0) == '_')
        {
            _ob_left = 0;
            _ob_right = _ob_names.length;
            String _ob_ami_op =
                opName.substring(1);

            while(_ob_left < _ob_right)
            {
                int _ob_m = (_ob_left + _ob_right) / 2;
                int _ob_res = _ob_names[_ob_m].compareTo(_ob_ami_op);
                if(_ob_res == 0)
                {
                    _ob_index = _ob_m;
                    break;
                }
                else if(_ob_res > 0)
                    _ob_right = _ob_m;
                else
                    _ob_left = _ob_m + 1;
            }
        }

        switch(_ob_index)
        {
        case 0: // establish_link
            return _OB_op_establish_link(in, handler);
        }

        throw new BAD_OPERATION();
    }

    private OutputStream
    _OB_op_establish_link(InputStream in,
                          ResponseHandler handler)
    {
        OutputStream out = null;
        try
        {
            String _ob_a0 = in.read_string();
            String _ob_a1 = in.read_string();
            int _ob_a2 = ProcessIDHelper.read(in);
            ProcessEndpoint _ob_a3 = ProcessEndpointHelper.read(in);
            establish_link(_ob_a0, _ob_a1, _ob_a2, _ob_a3);
            out = handler.createReply();
        }
        catch(AlreadyLinked _ob_ex)
        {
            out = handler.createExceptionReply();
            AlreadyLinkedHelper.write(out, _ob_ex);
        }
        return out;
    }
}
