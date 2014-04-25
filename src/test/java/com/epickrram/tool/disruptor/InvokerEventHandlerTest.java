package com.epickrram.tool.disruptor;

import com.epickrram.tool.disruptor.reflect.ObjectArrayHolder;
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
        proxyMethodInvocation.setArgumentHolder(new ObjectArrayHolder());
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
        proxyMethodInvocation.setArgumentHolder(new ObjectArrayHolder());
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
}