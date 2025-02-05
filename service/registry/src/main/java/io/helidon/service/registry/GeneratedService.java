/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates.
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.helidon.common.GenericType;
import io.helidon.common.types.ElementKind;
import io.helidon.common.types.TypeName;

/**
 * All types in this class are used from generated code for services.
 */
public final class GeneratedService {
    private GeneratedService() {
    }

    /**
     * Each descriptor for s service that is implements {@link Service.QualifiedFactory}
     * implements this interface to provide information about the qualifier it supports.
     */
    public interface QualifiedFactoryDescriptor {
        /**
         * Type of qualifier a {@link Service.QualifiedFactory} provides.
         *
         * @return type name of the qualifier this qualified factory can provide instances for
         */
        TypeName qualifierType();
    }

    /**
     * Each descriptor for s service that is annotated with {@link Service.PerInstance}
     * implements this interface to provide information about the type that drives it.
     */
    public interface PerInstanceDescriptor {
        /**
         * Service instances may be created for instances of another service.
         * If a type is created for another type, it inherits ALL qualifiers of the type that it is based on.
         *
         * @return create for service type
         */
        TypeName createFor();
    }

    /**
     * Each descriptor for a service that implements {@link Service.ScopeHandler}
     * implements this interface to provide information about the scope it handles.
     */
    public interface ScopeHandlerDescriptor {
        /**
         * Scope handled by the scope handler service.
         *
         * @return type of the scope handled (annotation)
         */
        TypeName handledScope();
    }

    /**
     * For event observers an observer registration is generated, so it can be picked-up by the
     * {@link EventManager} implementation at runtime.
     */
    @Service.Contract
    public interface EventObserverRegistration {
        /**
         * Register with the event manager.
         *
         * @param manager event manager
         */
        void register(EventManager manager);
    }

    /**
     * Utility type to provide method to combine injection point information for inheritance support.
     */
    public static final class DependencySupport {
        private DependencySupport() {
        }

        /**
         * Combine dependencies from this type with dependencies from supertype.
         * This is a utility for code generated types.
         *
         * @param myType    this type's dependencies
         * @param superType super type's dependencies
         * @return a new list without constructor dependencies from super type
         */
        public static List<Dependency> combineDependencies(List<Dependency> myType, List<Dependency> superType) {
            List<Dependency> result = new ArrayList<>(myType);

            // always inject all fields
            result.addAll(superType.stream()
                                  .filter(it -> it.elementKind() == ElementKind.FIELD)
                                  .toList());
            // ignore constructors, as we only need to inject constructor on the instantiated type

            // and only add methods that are not already injected on existing type
            Set<String> injectedMethods = myType.stream()
                    .filter(it -> it.elementKind() == ElementKind.METHOD)
                    .map(Dependency::method)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());

            result.addAll(superType.stream()
                                  .filter(it -> it.elementKind() == ElementKind.METHOD)
                                  .filter(it -> it.method().isPresent())
                                  .filter(it -> injectedMethods.add(it.method().get())) // we check presence above
                                  .toList());

            return List.copyOf(result);
        }
    }

    /**
     * Intercepted wrapper for generated interception delegates.
     *
     * @param <T> type of the provided contratc
     */
    abstract static class InterceptionWrapper<T> {
        /**
         * Wrap a qualified instance so the actual instance is correctly intercepted.
         *
         * @param qualifiedInstance qualified instance created by appropriate factory
         * @return qualified instance with wrapped instance
         */
        protected Service.QualifiedInstance<T> wrapQualifiedInstance(Service.QualifiedInstance<T> qualifiedInstance) {
            return Service.QualifiedInstance.create(wrap(qualifiedInstance.get()), qualifiedInstance.qualifiers());
        }

        /**
         * Wrap the instance for interception.
         * This method is code generated.
         *
         * @param originalInstance instance to wrap
         * @return wrapped instance
         */
        protected abstract T wrap(T originalInstance);
    }

    /**
     * Wrapper for generated Service factories that implement a {@link java.util.function.Supplier} of a service.
     *
     * @param <T> type of the provided contract
     */
    public abstract static class SupplierFactoryInterceptionWrapper<T> extends InterceptionWrapper<T>
            implements Supplier<T> {
        private final Supplier<T> delegate;

        /**
         * Creates a new instance delegating service instantiation to the provided supplier.
         *
         * @param delegate used to obtain service instance that will be {@link #wrap(Object) wrapped} for interception
         */
        protected SupplierFactoryInterceptionWrapper(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            return wrap(delegate.get());
        }
    }

    /**
     * Wrapper for generated Service factories that implement a
     * {@link Service.ServicesFactory}.
     *
     * @param <T> type of the provided contract
     */
    public abstract static class ServicesFactoryInterceptionWrapper<T> extends InterceptionWrapper<T>
            implements Service.ServicesFactory<T> {
        private final Service.ServicesFactory<T> delegate;

        /**
         * Creates a new instance delegating service instantiation to the provided services factory.
         *
         * @param delegate used to obtain service instances that will be {@link #wrap(Object) wrapped} for interception
         */
        protected ServicesFactoryInterceptionWrapper(Service.ServicesFactory<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Service.QualifiedInstance<T>> services() {
            return delegate.services()
                    .stream()
                    .map(this::wrapQualifiedInstance)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    /**
     * Wrapper for generated Service factories that implement a
     * {@link Service.InjectionPointFactory}.
     *
     * @param <T> type of the provided contract
     */
    public abstract static class IpFactoryInterceptionWrapper<T> extends InterceptionWrapper<T>
            implements Service.InjectionPointFactory<T> {
        private final Service.InjectionPointFactory<T> delegate;

        /**
         * Creates a new instance delegating service instantiation to the provided injection point factory.
         *
         * @param delegate used to obtain service instances that will be {@link #wrap(Object) wrapped} for interception
         */
        protected IpFactoryInterceptionWrapper(Service.InjectionPointFactory<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Service.QualifiedInstance<T>> list(Lookup lookup) {
            return delegate.first(lookup)
                    .stream()
                    .map(this::wrapQualifiedInstance)
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public Optional<Service.QualifiedInstance<T>> first(Lookup lookup) {
            return delegate.first(lookup)
                    .map(this::wrapQualifiedInstance);
        }
    }

    /**
     * Wrapper for generated Service factories that implement a
     * {@link Service.QualifiedFactory}.
     *
     * @param <T> type of the provided contract
     * @param <A> type of the qualifier annotation
     */
    public abstract static class QualifiedFactoryInterceptionWrapper<T, A extends Annotation> extends InterceptionWrapper<T>
            implements Service.QualifiedFactory<T, A> {
        private final Service.QualifiedFactory<T, A> delegate;

        /**
         * Creates a new instance delegating service instantiation to the provided qualified factory.
         *
         * @param delegate used to obtain service instances that will be {@link #wrap(Object) wrapped} for interception
         */
        protected QualifiedFactoryInterceptionWrapper(Service.QualifiedFactory<T, A> delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Service.QualifiedInstance<T>> list(Qualifier qualifier, Lookup lookup, GenericType<T> type) {
            return delegate.list(qualifier, lookup, type)
                    .stream()
                    .map(this::wrapQualifiedInstance)
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public Optional<Service.QualifiedInstance<T>> first(Qualifier qualifier, Lookup lookup, GenericType<T> type) {
            return delegate.first(qualifier, lookup, type)
                    .map(this::wrapQualifiedInstance);
        }
    }
}
