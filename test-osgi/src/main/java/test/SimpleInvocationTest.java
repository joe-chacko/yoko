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
package test;

import org.junit.jupiter.api.Test;
import testify.iiop.annotation.ConfigureServer;
import testify.iiop.annotation.ConfigureServer.RemoteImpl;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ConfigureServer
public class SimpleInvocationTest {
    public interface Echo extends Remote, Serializable {
        <T> T echo(T value) throws RemoteException;
    }

    @RemoteImpl
    public static final Echo IMPL = SimpleInvocationTest::echo;

    static <T> T echo(T t) { return t;}

    @Test
    public void testSendString(Echo stub) throws Exception {
        String expected = "Lorem ipsum dolor sit";
        String actual = stub.echo(expected);
        assertEquals(expected, actual);
    }

    @Test
    public void testSendDate(Echo stub) throws Exception {
        Date expected = new Date(0xcafebabefeedfaceL);
        Date actual = stub.echo(expected);
        assertEquals(expected, actual);
    }
}
