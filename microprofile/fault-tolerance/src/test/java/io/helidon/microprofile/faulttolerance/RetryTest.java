/*
 * Copyright (c) 2018, 2025 Oracle and/or its affiliates.
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

package io.helidon.microprofile.faulttolerance;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.helidon.microprofile.testing.junit5.AddBean;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Test cases for @Retry.
 */
@AddBean(RetryBean.class)
@AddBean(SyntheticRetryBean.class)
class RetryTest extends FaultToleranceTest {

    static Stream<Arguments> createBeans() {
        return Stream.of(
                Arguments.of((Supplier<RetryBean>) () -> newBean(RetryBean.class), "ManagedRetryBean"),
                Arguments.of((Supplier<RetryBean>) () -> newNamedBean(SyntheticRetryBean.class), "SyntheticRetryBean"));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryBean(Supplier<RetryBean> supplier, String unused) {
        RetryBean bean = supplier.get();
        bean.reset();
        assertThat(bean.getInvocations(), is(0));
        bean.retry();
        assertThat(bean.getInvocations(), is(3));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryBeanFallback(Supplier<RetryBean> supplier, String unused) {
        RetryBean bean = supplier.get();
        bean.reset();
        assertThat(bean.getInvocations(), is(0));
        String value = bean.retryWithFallback();
        assertThat(bean.getInvocations(), is(2));
        assertThat(value, is("fallback"));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryAsync(Supplier<RetryBean> supplier, String unused) throws Exception {
        RetryBean bean = supplier.get();
        bean.reset();
        CompletableFuture<String> future = bean.retryAsync();
        future.get();
        assertThat(bean.getInvocations(), is(3));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryWithDelayAndJitter(Supplier<RetryBean> supplier, String unused) {
        RetryBean bean = supplier.get();
        bean.reset();
        long millis = System.currentTimeMillis();
        bean.retryWithDelayAndJitter();
        assertThat(System.currentTimeMillis() - millis, greaterThan(200L));
    }

    /**
     * Inspired by a TCK test which makes sure failed executions propagate correctly.
     *
     * @param supplier supplier of the bean to invoke
     * @param unused bean name to use for the specific test invocation
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryWithException(Supplier<RetryBean> supplier, String unused) {
        RetryBean bean = supplier.get();
        bean.reset();
        CompletionStage<String> future = bean.retryWithException();
        assertCompleteExceptionally(future.toCompletableFuture(), IOException.class, "Simulated error");
        assertThat(bean.getInvocations(), is(3));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryCompletionStageWithEventualSuccess(Supplier<RetryBean> supplier, String unused) {
        RetryBean bean = supplier.get();
        bean.reset();
        assertCompleteOk(bean.retryWithUltimateSuccess(), "success");
        assertThat(bean.getInvocations(), is(3));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createBeans")
    void testRetryWithCustomRuntimeException(Supplier<RetryBean> supplier, String unused) {
        RetryBean bean = supplier.get();
        bean.reset();
        assertThat(bean.getInvocations(), is(0));
        assertCompleteOk(bean.retryOnCustomRuntimeException(), "success");
        assertThat(bean.getInvocations(), is(3));
    }
}
