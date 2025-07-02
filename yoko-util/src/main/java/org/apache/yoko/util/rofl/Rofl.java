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
package org.apache.yoko.util.rofl;

import org.apache.yoko.util.HexConverter;

import java.io.Serializable;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.Arrays.copyOf;
import static java.util.logging.Level.WARNING;
import static org.apache.yoko.util.rofl.Rofl.RemoteOrb.BAD;
import static org.apache.yoko.util.rofl.Rofl.RemoteOrb.IBM;
import static org.apache.yoko.util.rofl.Rofl.RemoteOrb.NO_DATA;

/**
 * <h1>ROFL &mdash; RemoteOrbFinessingLogic</h1>
 * This class encapsulates all the fixes that affect the stream format when talking to other ORBs.
 * These will be read in from two sources:
 * <ul>
 *     <li>a component tag in an IOR profile</li>
 *     <li>a service context in a GIOP packet</li>
 * </ul>
 * and referred to only when marshalling data.
 */
public interface Rofl extends Serializable {
    Rofl NONE = new None();
    enum RemoteOrb {
        /** The IBM Java ORB */
        IBM(0x49424D0A, 0x49424D12, IbmPartnerVersion::new),
        BAD,
        NO_DATA
        ;

        public final Integer tagComponentId;
        public final Integer serviceContextId;
        private final Function<byte[], Rofl> ctor;
        RemoteOrb() { this(null, null, null); }
        RemoteOrb(Integer tagComponentId, Integer serviceContextId, Function<byte[], Rofl> ctor) {
            this.tagComponentId = tagComponentId;
            this.serviceContextId = serviceContextId;
            this.ctor = ctor;
        }

        public Rofl createRofl(byte[] data) {
            try {
                return ctor.apply(data);
            } catch (Throwable t) {
                Logger.getLogger(Rofl.class.getName() + "." + name()).log(WARNING, "Failed to create ROFL for remote ORB of type " + this, t);
                return new Bad(data, t);
            }
        }
    }
    RemoteOrb type();

    final class IbmPartnerVersion implements Rofl{
        private static final long serialVersionUID = 1L;
        public final short major, minor, extended;
        IbmPartnerVersion(byte[] data) {
            if (data.length != 8) {
                major = minor = extended = -1;
                return;
            }
            int i = 0;
            // 1 byte boolean: true iff littleEndian - ignore since Java is big-endian
            i++;
            // 1 byte padding - ignore
            i++;
            // extended short
            extended = (short)(((data[i++] & 0xFF) << 8) | (data[i++] & 0xFF));
            // major short
            major = (short)(((data[i++] & 0xFF) << 8) | (data[i++] & 0xFF));
            // minor short
            minor = (short)(((data[i++] & 0xFF) << 8) | (data[i++] & 0xFF));
        }
        public RemoteOrb type() { return IBM; }
        public String toString() { return String.format("IBM JAVA ORB[major=%04X minor=%04x, extended=%04X]", major, minor, extended); }
    }

    final class Bad implements Rofl {
        private static final long serialVersionUID = 1L;
        byte[] data;
        Throwable cause;
        Bad(byte[] data, Throwable cause) { this.data = data == null ? null : copyOf(data, data.length); }
        public RemoteOrb type()  { return BAD; }
        public String toString() { return String.format("UNKNOWN ORB[%s]", HexConverter.octetsToAscii(data)); }
    }

    final class None implements Rofl {
        public RemoteOrb type() { return NO_DATA; }
        public String toString() { return "NONE"; }
    }
}
