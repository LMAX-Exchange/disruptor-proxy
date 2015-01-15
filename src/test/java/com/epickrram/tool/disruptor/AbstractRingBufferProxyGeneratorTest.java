package com.epickrram.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
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
        final Listener listener = ringBufferProxyGenerator.createRingBufferProxy(implementation, Listener.class, disruptor, OverflowStrategy.DROP);
        disruptor.start();

        for(int i = 0; i < 3; i++)
        {
            listener.onString("single string " + i);
            listener.onFloatAndInt((float) i, i);
            listener.onVoid();
            listener.onObjectArray(new Double[]{(double) i});
            listener.onMixedMultipleArgs(0, 1, "a", "b", 2);
        }

        disruptor.shutdown();
        executor.shutdown();

        assertThat(implementation.getLastStringValue(), is("single string 2"));
        assertThat(implementation.getLastFloatValue(), is((float) 2));
        assertThat(implementation.getLastIntValue(), is(2));
        assertThat(implementation.getVoidInvocationCount(), is(3));
        assertThat(implementation.getMixedArgsInvocationCount(), is(3));
        assertThat(implementation.getLastDoubleArray(), is(equalTo(new Double[] {(double) 2})));
    }

    @Test
    public void shouldProxyMultipleImplementations()
    {
        final ExecutorService executor = Executors.newCachedThreadPool();
        final Disruptor<ProxyMethodInvocation> disruptor =
                new Disruptor<ProxyMethodInvocation>(new RingBufferProxyEventFactory(), 1024, executor);
        final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();
        final RingBufferProxyGenerator ringBufferProxyGenerator = generatorFactory.create(generatorType);

        final ListenerImpl[] implementations = new ListenerImpl[]
        {
            new ListenerImpl(), new ListenerImpl()
        };

        final Listener listener = ringBufferProxyGenerator.createRingBufferProxy(Listener.class, disruptor, OverflowStrategy.DROP, implementations);
        disruptor.start();

        for(int i = 0; i < 3; i++)
        {
            listener.onString("single string " + i);
            listener.onFloatAndInt((float) i, i);
            listener.onVoid();
            listener.onObjectArray(new Double[]{(double) i});
            listener.onMixedMultipleArgs(0, 1, "a", "b", 2);
        }

        disruptor.shutdown();
        executor.shutdown();

        for (ListenerImpl implementation : implementations)
        {
            assertThat(implementation.getLastStringValue(), is("single string 2"));
            assertThat(implementation.getLastFloatValue(), is((float) 2));
            assertThat(implementation.getLastIntValue(), is(2));
            assertThat(implementation.getVoidInvocationCount(), is(3));
            assertThat(implementation.getMixedArgsInvocationCount(), is(3));
            assertThat(implementation.getLastDoubleArray(), is(equalTo(new Double[] {(double) 2})));
        }
    }

    @Test
    public void shouldDropMessagesIfRingBufferIsFull() throws Exception
    {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Disruptor<ProxyMethodInvocation> disruptor =
                new Disruptor<ProxyMethodInvocation>(new RingBufferProxyEventFactory(), 4, executor);
        final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();
        final RingBufferProxyGenerator ringBufferProxyGenerator = generatorFactory.create(generatorType);

        final CountDownLatch latch = new CountDownLatch(1);
        final BlockingOverflowTest implementation = new BlockingOverflowTest(latch);
        final OverflowTest listener = ringBufferProxyGenerator.createRingBufferProxy(implementation, OverflowTest.class, disruptor, OverflowStrategy.DROP);
        disruptor.start();

        for(int i = 0; i < 8; i++)
        {
            listener.invoke();
        }

        latch.countDown();

        Thread.sleep(250L);

        disruptor.shutdown();
        executor.shutdown();

        assertThat(implementation.getInvocationCount(), is(4));
    }

    @Test
    public void shouldNotifyBatchListenerImplementationOfEndOfBatch() throws Exception
    {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Disruptor<ProxyMethodInvocation> disruptor =
                new Disruptor<ProxyMethodInvocation>(new RingBufferProxyEventFactory(), 4, executor);
        final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();
        final RingBufferProxyGenerator ringBufferProxyGenerator = generatorFactory.create(generatorType);

        final BatchAwareListenerImpl implementation = new BatchAwareListenerImpl();
        final Listener listener = ringBufferProxyGenerator.createRingBufferProxy(implementation, Listener.class, disruptor, OverflowStrategy.DROP);
        disruptor.start();

        listener.onString("foo1");
        listener.onString("foo2");
        listener.onString("foo3");
        listener.onString("foo4");


        long timeoutAt = System.currentTimeMillis() + 2000L;

        while(implementation.getBatchCount() == 0 && System.currentTimeMillis() < timeoutAt)
        {
            Thread.sleep(1);
        }

        final int firstBatchCount = implementation.getBatchCount();
        assertThat(firstBatchCount, is(not(0)));

        listener.onVoid();
        listener.onVoid();
        listener.onVoid();

        timeoutAt = System.currentTimeMillis() + 2000L;

        while(implementation.getBatchCount() == firstBatchCount && System.currentTimeMillis() < timeoutAt)
        {
            Thread.sleep(1);
        }

        disruptor.shutdown();
        executor.shutdown();

        assertThat(implementation.getBatchCount() > firstBatchCount, is(true));
    }

    private static final class BlockingOverflowTest implements OverflowTest
    {
        private final CountDownLatch blocker;
        private final AtomicInteger invocationCount = new AtomicInteger(0);

        private BlockingOverflowTest(final CountDownLatch blocker)
        {
            this.blocker = blocker;
        }

        @Override
        public void invoke()
        {
            try
            {
                blocker.await();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException();
            }
            invocationCount.incrementAndGet();
        }

        int getInvocationCount()
        {
            return invocationCount.get();
        }
    }

    public interface OverflowTest
    {
        void invoke();
    }
}
