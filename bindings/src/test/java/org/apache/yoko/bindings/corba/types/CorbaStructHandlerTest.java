/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.yoko.bindings.corba.types;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.schemas.yoko.bindings.corba.MemberType;
import org.apache.schemas.yoko.bindings.corba.Struct;
import org.apache.yoko.wsdl.CorbaConstants;

import org.omg.CORBA.ORB;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

// Since the exception handler is essentially the same as the struct handler (just included in case 
// structs and exceptions diverge at a later date), this test should cover both.
public class CorbaStructHandlerTest extends TestCase {

    private ORB orb;
    
    public CorbaStructHandlerTest(String arg0) {
        super(arg0);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(CorbaObjectHandlerTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        java.util.Properties props = System.getProperties();
        props.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");
        props.put("yoko.orb.id", "Yoko-Server-Binding");
        orb = ORB.init(new String[0], props);
    }
    
    protected void tearDown() throws Exception {
        if (orb != null) {
            try {
                orb.destroy();
            } catch (Exception ex) {
                // Do nothing.  Throw an Exception?
            }
        }
    }
    
    public void testCorbaStructHandler() {
        Struct structType = new Struct();
        structType.setName("TestStruct");
        structType.setRepositoryID("IDL:TestStruct:1.0");
        MemberType member0 = new MemberType();
        member0.setIdltype(CorbaConstants.NT_CORBA_LONG);
        member0.setName("member0");
        MemberType member1 = new MemberType();
        member1.setIdltype(CorbaConstants.NT_CORBA_STRING);
        member1.setName("member1");
        
        QName structName = new QName("TestStruct");
        QName structIdlType = 
            new QName(CorbaConstants.NU_WSDL_CORBA, "testStruct", CorbaConstants.NP_WSDL_CORBA);
        StructMember[] structMembers = new StructMember[2];
        structMembers[0] = new StructMember("member0", 
                                            orb.get_primitive_tc(TCKind.tk_long),
                                            null);
        structMembers[1] = new StructMember("member1", 
                        orb.get_primitive_tc(TCKind.tk_string),
                        null);
        TypeCode structTC = orb.create_struct_tc(structType.getRepositoryID(), 
                                                 structType.getName(),
                                                 structMembers);
        
        CorbaStructHandler obj = new CorbaStructHandler(structName, structIdlType, structTC, structType);
        assertNotNull(obj);
        
        CorbaPrimitiveHandler objMember0 = new CorbaPrimitiveHandler(new QName(member0.getName()),
                                                                     member0.getIdltype(),
                                                                     orb.get_primitive_tc(TCKind.tk_long),
                                                                     null);
        assertNotNull(objMember0);
        obj.addMember(objMember0);
        
        CorbaPrimitiveHandler objMember1 = new CorbaPrimitiveHandler(new QName(member1.getName()),
                                                                     member1.getIdltype(),
                                                                     orb.get_primitive_tc(TCKind.tk_string),
                                                                     null);
        assertNotNull(objMember1);
        obj.addMember(objMember1);

        int memberSize = obj.getMembers().size();
        assertTrue(memberSize == 2);
        
        QName nameResult = obj.getName();
        assertTrue(structName.equals(nameResult));
        
        QName idlTypeResult = obj.getIdlType();
        assertTrue(structIdlType.equals(idlTypeResult));
        
        CorbaObjectHandler member0Result = obj.getMemberByName("member0");
        assertNotNull(member0Result);
        assertTrue(member0Result.getName().equals(objMember0.getName()));
        
        CorbaObjectHandler member1Result = obj.getMember(1);
        assertNotNull(member1Result);
        assertTrue(member1Result.getName().equals(objMember1.getName()));        
    }
}