/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.service.registry;

import java.util.Set;
import java.util.function.Supplier;

import io.helidon.common.types.ResolvedType;
import io.helidon.common.types.TypeName;
import io.helidon.service.registry.Service.QualifiedInstance;

/*
Manager of a single service. There is one instance per service provider (and per service descriptor).
 */
class ServiceManager<T> {
    private final ServiceProvider<T> provider;
    private final boolean explicitInstance;
    private final Supplier<Activator<T>> activatorSupplier;
    private final CoreServiceRegistry registry;
    private final Supplier<Scope> scopeSupplier;

    ServiceManager(CoreServiceRegistry registry,
                   Supplier<Scope> scopeSupplier,
                   ServiceProvider<T> provider,
                   boolean explicitInstance,
                   Supplier<Activator<T>> activatorSupplier) {
        this.registry = registry;
        this.scopeSupplier = scopeSupplier;
        this.provider = provider;
        this.explicitInstance = explicitInstance;
        this.activatorSupplier = activatorSupplier;
    }

    @Override
    public String toString() {
        return provider.descriptor().serviceType().classNameWithEnclosingNames();
    }

    void ensureBindingPlan() {
        if (explicitInstance) {
            // we do not need injection plan, if service was provided as an instance
            return;
        }
        registry.bindings()
                .bindingPlan(provider.descriptor())
                .ensure();
    }

    ServiceInstance<T> registryInstance(Lookup lookup, QualifiedInstance<T> instance) {
        return new ServiceInstanceImpl<>(provider.descriptor(),
                                         provider.contracts(lookup),
                                         instance);
    }

    ServiceInfo descriptor() {
        return provider.descriptor();
    }

    /*
    Get service activator for the scope it is in (always works for singleton, may fail for other)
    this provides an instance of an activator that is bound to a scope instance
    */
    Activator<T> activator() {
        return scopeSupplier
                .get()
                .registry()
                .activator(provider.descriptor(),
                           activatorSupplier);
    }

    private static final class ServiceInstanceImpl<T> implements ServiceInstance<T> {
        private final ServiceDescriptor<T> descriptor;
        private final QualifiedInstance<T> qualifiedInstance;
        private final Set<ResolvedType> contracts;

        private ServiceInstanceImpl(ServiceDescriptor<T> descriptor,
                                    Set<ResolvedType> contracts,
                                    QualifiedInstance<T> qualifiedInstance) {
            this.descriptor = descriptor;
            this.contracts = contracts;
            this.qualifiedInstance = qualifiedInstance;
        }

        @Override
        public T get() {
            return qualifiedInstance.get();
        }

        @Override
        public Set<Qualifier> qualifiers() {
            return qualifiedInstance.qualifiers();
        }

        @Override
        public Set<ResolvedType> contracts() {
            return contracts;
        }

        @Override
        public TypeName scope() {
            return descriptor.scope();
        }

        @Override
        public double weight() {
            return descriptor.weight();
        }

        @Override
        public TypeName serviceType() {
            return descriptor.serviceType();
        }

        @Override
        public String toString() {
            return "Instance of " + descriptor.serviceType().fqName() + ": " + qualifiedInstance;
        }
    }
}
