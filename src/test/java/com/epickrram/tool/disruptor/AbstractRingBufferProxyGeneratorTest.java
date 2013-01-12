package com.epickrram.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractRingBufferProxyGeneratorTest
{
    private final GeneratorType generatorType;

    protected AbstractRingBufferProxyGeneratorTest(final GeneratorType generatorType)
    {
        this.generatorType = generatorType;
    }

    @Test
    public void shouldProxy()
    {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Disruptor<ProxyMethodInvocation> disruptor =
                new Disruptor<ProxyMethodInvocation>(new RingBufferProxyEventFactory(), 1024, executor);
        final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();
        final RingBufferProxyGenerator ringBufferProxyGenerator = generatorFactory.create(generatorType);

        final ListenerImpl implementation = new ListenerImpl();
        final Listener listener = ringBufferProxyGenerator.createRingBufferProxy(implementation, Listener.class, disruptor);
        disruptor.start();

        for(int i = 0; i < 3; i++)
        {
            listener.onString("single string " + i);
            listener.onFloatAndInt((float) i, i);
            listener.onVoid();
            listener.onObjectArray(new Double[]{(double) i});
        }

        disruptor.shutdown();
        executor.shutdown();

        assertThat(implementation.getLastStringValue(), is("single string 2"));
        assertThat(implementation.getLastFloatValue(), is((float) 2));
        assertThat(implementation.getLastIntValue(), is(2));
        assertThat(implementation.getVoidInvocationCount(), is(3));
        assertThat(implementation.getLastDoubleArray(), is(equalTo(new Double[] {(double) 2})));
    }
}
