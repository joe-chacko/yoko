/*
 * Copyright 2026 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.apache.yoko;

import org.apache.yoko.osgi.ProviderLocator;
import org.apache.yoko.osgi.ProviderRegistry;
import org.apache.yoko.osgi.locator.ProviderRegistryImpl;
import org.apache.yoko.osgi.locator.Register;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

/**
 * Bundle activator for {@code io.openliberty.yoko.core}.
 * <p>
 * Registers a single {@link ProviderRegistryImpl} instance as both
 * {@link Register} and {@link ProviderRegistry} OSGi services, then
 * installs it into {@link ProviderLocator} so that static class-loading
 * code can find registered providers without holding a service reference.
 */
public final class Activator implements BundleActivator {

    private ProviderRegistryImpl registry;
    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) {
        registry = new ProviderRegistryImpl();

        Hashtable<String, Object> props = new Hashtable<>();
        props.put("service.vendor", "IBM");
        registration = context.registerService(
                new String[]{ Register.class.getName(), ProviderRegistry.class.getName() },
                registry,
                props);

        ProviderLocator.setRegistry(registry);
    }

    @Override
    public void stop(BundleContext context) {
        ProviderLocator.setRegistry(null);
        registration.unregister();
        registration = null;
        registry = null;
    }
}
