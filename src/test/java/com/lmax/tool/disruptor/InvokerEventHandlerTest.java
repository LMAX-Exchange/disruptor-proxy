/*
 * Copyright 2015 LMAX Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.lmax.tool.disruptor;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public final class InvokerEventHandlerTest
{
    @Test
    public void shouldNotifyBatchListenerOnEndOfBatch() throws Exception
    {
        final BatchAwareListenerImpl batchAwareListener = new BatchAwareListenerImpl();
        final InvokerEventHandler<Listener> eventHandler = new InvokerEventHandler<Listener>(batchAwareListener);
        final ProxyMethodInvocation proxyMethodInvocation = new ProxyMethodInvocation();
        proxyMethodInvocation.setArgumentHolder(new StubResetable());
        proxyMethodInvocation.setInvoker(new NoOpInvoker());

        eventHandler.onEvent(proxyMethodInvocation, 17L, true);

        assertThat(batchAwareListener.getBatchCount(), is(1));
    }

    @Test
    public void shouldNotNotifyNonBatchListenerOnEndOfBatch() throws Exception
    {
        final ListenerImpl batchAwareListener = new ListenerImpl();
        final InvokerEventHandler<Listener> eventHandler = new InvokerEventHandler<Listener>(batchAwareListener);
        final ProxyMethodInvocation proxyMethodInvocation = new ProxyMethodInvocation();
        proxyMethodInvocation.setArgumentHolder(new StubResetable());
        proxyMethodInvocation.setInvoker(new NoOpInvoker());

        eventHandler.onEvent(proxyMethodInvocation, 17L, true);
    }

    private static class NoOpInvoker implements Invoker
    {
        @Override
        public void invokeWithArgumentHolder(final Object implementation, final Object argumentHolder)
        {
        }
    }

    private static final class StubResetable implements Resetable
    {
        @Override
        public void reset()
        {

        }
    }
}