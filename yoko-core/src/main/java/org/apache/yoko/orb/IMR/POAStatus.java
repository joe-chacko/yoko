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

import static org.omg.CORBA.CompletionStatus.COMPLETED_NO;

import java.io.ObjectStreamException;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.portable.IDLEntity;

//
// IDL:orb.yoko.apache.org/IMR/POAStatus:1.0
//
/**
 *
 * This enumeration contains the various states of a server.
 *
 **/

public class POAStatus implements IDLEntity
{
    private static POAStatus [] values_ = new POAStatus[5];
    private int value_;

    public final static int _INACTIVE = 0;
    public final static POAStatus INACTIVE = new POAStatus(_INACTIVE);
    public final static int _ACTIVE = 1;
    public final static POAStatus ACTIVE = new POAStatus(_ACTIVE);
    public final static int _HOLDING = 2;
    public final static POAStatus HOLDING = new POAStatus(_HOLDING);
    public final static int _DISCARDING = 3;
    public final static POAStatus DISCARDING = new POAStatus(_DISCARDING);
    public final static int _NON_EXISTENT = 4;
    public final static POAStatus NON_EXISTENT = new POAStatus(_NON_EXISTENT);

    protected
    POAStatus(int value)
    {
        values_[value] = this;
        value_ = value;
    }

    public int
    value()
    {
        return value_;
    }

    public static POAStatus
    from_int(int value)
    {
        if(value < values_.length)
            return values_[value];
        else
            throw new BAD_PARAM("Value (" + value  + ") out of range", 25, COMPLETED_NO);
    }

    private Object
    readResolve()
        throws ObjectStreamException
    {
        return from_int(value());
    }
}
