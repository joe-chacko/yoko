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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.schemas.yoko.bindings.corba.TypeMappingType;
import org.apache.yoko.bindings.corba.CorbaBindingFactory;
import org.apache.yoko.bindings.corba.CorbaBinding;
import org.apache.yoko.bindings.corba.CorbaDestination;
import org.apache.yoko.bindings.corba.CorbaTypeMap;
import org.apache.yoko.bindings.corba.TestUtils;
import org.apache.yoko.bindings.corba.utils.CorbaUtils;
import org.apache.yoko.wsdl.CorbaConstants;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.wsdl11.WSDLServiceFactory;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.omg.CORBA.ORB;

import junit.framework.TestCase;

public class CorbaHandlerUtilsTest extends TestCase {

    private final String complexTypesNamespaceURI = "http://yoko.apache.org/ComplexTypes/idl_types";
    private final String complexTypesPrefix = "corbatm";    
    private ORB orb;
    private Bus bus;    
    protected EndpointInfo endpointInfo;
    BindingFactory factory;
    CorbaTypeMap typeMap;
    ServiceInfo service;
    
    public CorbaHandlerUtilsTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CorbaHandlerUtilsTest.class);
    }    
    
    protected void setUp() throws Exception {
        super.setUp();

        bus = BusFactory.newInstance().getDefaultBus();              
        BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);        
        factory = (BindingFactory)bfm.getBindingFactory("http://schemas.apache.org/yoko/bindings/corba");
        bfm.registerBindingFactory(CorbaConstants.NU_WSDL_CORBA, factory);        

        java.util.Properties props = System.getProperties();
        props.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
        props.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");
        props.put("yoko.orb.id", "Yoko-Server-Binding");
        orb = ORB.init(new String[0], props);
        
        TestUtils testUtils = new TestUtils();
        //CorbaDestination destination = (CorbaDestination)getDestination();
        CorbaDestination destination = testUtils.getComplexTypesTestDestination();
        service = destination.getBindingInfo().getService();
        List<TypeMappingType> corbaTypes = service.getDescription().getExtensors(TypeMappingType.class);        
        typeMap = CorbaUtils.createCorbaTypeMap(corbaTypes);
    }
    
    protected void tearDown() throws Exception {
        bus.shutdown(true); 
        if (orb != null) {
            try {
                orb.destroy();
            } catch (Exception ex) {
                // Do nothing.  Throw an Exception?
            }
        } 
    }
            
    /*protected void setupServiceInfo(String ns, String wsdl, String serviceName, String portName) {        
        URL wsdlUrl = getClass().getResource(wsdl);
        assertNotNull(wsdlUrl);
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, wsdlUrl, new QName(ns, serviceName));

        Service service = factory.create();        
        endpointInfo = service.getEndpointInfo(new QName(ns, portName));
   
    }
    
    public Destination getDestination() throws Exception {    
        setupServiceInfo("http://yoko.apache.org/ComplexTypes", 
                         "/wsdl/ComplexTypes.wsdl", 
                         "ComplexTypesCORBAService", 
                         "ComplexTypesCORBAPort");
        CorbaBindingFactory corbaBF = (CorbaBindingFactory)factory;
        Destination destination = corbaBF.getDestination(endpointInfo);
        assertNotNull(destination);
        return destination;
    }*/
    
    public void testCreateTypeHandler() {
        QName objName = null;
        QName objIdlType = null;
        CorbaObjectHandler result = null;     
        
        // Test for an array handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestArray", complexTypesPrefix);        
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaArrayHandler);

        // Test for an enum handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestEnum", complexTypesPrefix);
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaEnumHandler);

        // Test for a fixed handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestFixed", complexTypesPrefix);
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaFixedHandler);

        // Test for a primitive handler
        objName = new QName("object");
        objIdlType = CorbaConstants.NT_CORBA_BOOLEAN;  
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaPrimitiveHandler);

        // Test for a sequence handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestSequence", complexTypesPrefix);
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaSequenceHandler);

        // Test for a struct handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestStruct", complexTypesPrefix);
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaStructHandler);

        // Test for a union handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestUnion", complexTypesPrefix);
        result = CorbaHandlerUtils.createTypeHandler(orb, objName, objIdlType, typeMap);
        assertTrue(result instanceof CorbaUnionHandler);
    }
    
    public void testInitializeObjectHandler() {        
        QName objName = null;
        QName objIdlType = null;
        CorbaObjectHandler result = null;        

        // Test for an array handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestArray", complexTypesPrefix);
        result = CorbaHandlerUtils.initializeObjectHandler(orb, objName, objIdlType, typeMap, service);
        assertTrue(result instanceof CorbaArrayHandler);
        CorbaArrayHandler arrayHandler = (CorbaArrayHandler)result;
        // WSDL defines the array to have 5 elements
        assertTrue(arrayHandler.getElements().size() == 5);
        

        // Test for a sequence handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestSequence", complexTypesPrefix);
        result = CorbaHandlerUtils.initializeObjectHandler(orb, objName, objIdlType, typeMap, service);
        assertTrue(result instanceof CorbaSequenceHandler);
        CorbaSequenceHandler seqHandler = (CorbaSequenceHandler)result;
        // This is an unbounded sequence so make sure there are no elements and the template
        // element has been set.
        assertTrue(seqHandler.getElements().size() == 0);
        assertNotNull(seqHandler.getTemplateElement());
        
        // Test for a bounded sequence handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestBoundedSequence", complexTypesPrefix);
        result = CorbaHandlerUtils.initializeObjectHandler(orb, objName, objIdlType, typeMap, service);
        assertTrue(result instanceof CorbaSequenceHandler);
        CorbaSequenceHandler boundedSeqHandler = (CorbaSequenceHandler)result;
        // This is a bounded sequence with WSDL defining 5 elements.
        assertTrue(boundedSeqHandler.getElements().size() == 5);
        
        // Test for a struct handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestStruct", complexTypesPrefix);
        result = CorbaHandlerUtils.initializeObjectHandler(orb, objName, objIdlType, typeMap, service);
        assertTrue(result instanceof CorbaStructHandler);
        CorbaStructHandler structHandler = (CorbaStructHandler)result;
        // The WSDL defines this struct as having three members
        assertTrue(structHandler.getMembers().size() == 3);

        // Test for a union handler
        objName = new QName("object");
        objIdlType = new QName(complexTypesNamespaceURI, "TestUnion", complexTypesPrefix);
        result = CorbaHandlerUtils.initializeObjectHandler(orb, objName, objIdlType, typeMap, service);
        assertTrue(result instanceof CorbaUnionHandler);
    }
}