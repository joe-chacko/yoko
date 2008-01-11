/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

package org.apache.yoko.tools.processors.idl;

import javax.xml.namespace.QName;

import antlr.collections.AST;

import org.apache.schemas.yoko.bindings.corba.Exception;
import org.apache.schemas.yoko.bindings.corba.MemberType;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

import org.apache.yoko.tools.common.ReferenceConstants;
import org.apache.yoko.wsdl.CorbaTypeImpl;

public class ExceptionVisitor extends VisitorBase {

    private static final String TYPE_SUFFIX = "Type";
    
    public ExceptionVisitor(Scope scope,
                            WSDLASTVisitor wsdlASTVisitor) {
        super(scope, wsdlASTVisitor);
    }

    public static boolean accept(AST node) {
        if (node.getType() == IDLTokenTypes.LITERAL_exception) {
            return true;
        }
        return false;
    }
    
    public void visit(AST node) {
        // <exception_dcl> ::= "exception" <identifier> "{" <member>* "}"
        // <member> ::= <type_spec> <declarators> ";"

        // <type_spec> visited by TypesVisitor
        
        // Following should be visited by a separate visitor
        // <declarators> ::= <declarator> { "," <declarator> }*
        // <declarator> ::= <simple_declarator>
        //                | <complex_declarator>
        // <simple_declarator> ::= <identifier>
        // <complex_declarator> ::= <array_declarator>
        // <array_declarator> ::= <identifier> <fixed_array_size>+
        // <fixed_array_size> ::= "[" <positive_int_const> "]"

        
        AST identifierNode = node.getFirstChild();
        Scope exceptionScope = new Scope(getScope(), identifierNode);
        
        // xmlschema:exception
        Scope scopedName = new Scope(getScope(), identifierNode);
        String exceptionName = scopedName.toString();
        XmlSchemaElement element = new XmlSchemaElement();
        element.setName(exceptionName);
        element.setQName(new QName(schema.getTargetNamespace(), exceptionName));

        String exceptionTypeName = exceptionName + TYPE_SUFFIX;

        XmlSchemaComplexType complexType = new XmlSchemaComplexType(schema);
        complexType.setName(exceptionTypeName);
        //complexType.setQName(new QName(schema.getTargetNamespace(), exceptionTypeName));
        XmlSchemaSequence sequence = new XmlSchemaSequence();
        complexType.setParticle(sequence);

        element.setSchemaTypeName(complexType.getQName());
        
        // corba:exception
        Exception exception = new Exception();
        exception.setQName(new QName(typeMap.getTargetNamespace(), exceptionName));
        exception.setType(complexType.getQName());
        exception.setRepositoryID(scopedName.toIDLRepositoryID());

        
        // exception members
        AST memberTypeNode = identifierNode.getNextSibling();
        while (memberTypeNode != null) {
            AST memberNode = memberTypeNode.getNextSibling();

            TypesVisitor visitor = new TypesVisitor(exceptionScope,
                                                    wsdlVisitor,
                                                    null);
            visitor.visit(memberTypeNode);
            XmlSchemaType stype = visitor.getSchemaType();
            CorbaTypeImpl ctype = visitor.getCorbaType();
            Scope fullyQualifiedName = visitor.getFullyQualifiedName();
            
            // needed for anonymous arrays in exceptions
            if (ArrayVisitor.accept(memberNode)) {
                Scope anonScope = new Scope(exceptionScope, 
                                            TypesUtils.getCorbaTypeNameNode(memberTypeNode));
                ArrayVisitor arrayVisitor = new ArrayVisitor(anonScope,
                                                             wsdlVisitor,
                                                             stype,
                                                             ctype,
                                                             null,
                                                             fullyQualifiedName);
                arrayVisitor.visit(memberNode);
                stype = arrayVisitor.getSchemaType();
                ctype = arrayVisitor.getCorbaType();
            }

            
            XmlSchemaElement member = createElementType(memberNode, stype,
                                                        fullyQualifiedName);
            sequence.getItems().add(member);

            MemberType memberType = createMemberType(memberNode, ctype, 
                                                     fullyQualifiedName);
            exception.getMember().add(memberType);
            
            
            memberTypeNode = memberNode.getNextSibling();
        }

        schema.addType(complexType);
        schema.getItems().add(element);
        schema.getItems().add(complexType);
        
        
        // add exception to corba typemap
        typeMap.getStructOrExceptionOrUnion().add(exception);
        
        setSchemaType(complexType);
        setCorbaType(exception);
    }
    
    private XmlSchemaElement createElementType(AST memberNode, XmlSchemaType stype,
                                               Scope fqName) {
        // xmlschema:member
        XmlSchemaElement member = new XmlSchemaElement();
        String memberName = memberNode.toString();
        member.setName(memberName);
        if (stype != null) {
            member.setSchemaType(stype);
            member.setSchemaTypeName(stype.getQName());
            if (stype.getQName().equals(ReferenceConstants.WSADDRESSING_TYPE)) {
                member.setNillable(true);
            }
        } else {
            wsdlVisitor.getDeferredActions().
                add(new ExceptionDeferredAction(member, fqName)); 
        }
        return member;
    }
    
    private MemberType createMemberType(AST memberNode, CorbaTypeImpl ctype, 
                                        Scope fqName) {
        // corba:member
        MemberType memberType = new MemberType();
        memberType.setName(memberNode.toString());
        if (ctype != null) {
            memberType.setIdltype(ctype.getQName());
        } else {
            wsdlVisitor.getDeferredActions().
                add(new ExceptionDeferredAction(memberType, fqName));
        }
        
        return memberType;
    }
    
}