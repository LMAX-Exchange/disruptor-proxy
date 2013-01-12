//////////////////////////////////////////////////////////////////////////////////
//   Copyright 2011   Mark Price     mark at epickrram.com                      //
//                                                                              //
//   Licensed under the Apache License, Version 2.0 (the "License");            //
//   you may not use this file except in compliance with the License.           //
//   You may obtain a copy of the License at                                    //
//                                                                              //
//       http://www.apache.org/licenses/LICENSE-2.0                             //
//                                                                              //
//   Unless required by applicable law or agreed to in writing, software        //
//   distributed under the License is distributed on an "AS IS" BASIS,          //
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   //
//   See the License for the specific language governing permissions and        //
//   limitations under the License.                                             //
//////////////////////////////////////////////////////////////////////////////////


package com.epickrram.tool.disruptor;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.epickrram.tool.disruptor.GeneratorType.BYTECODE_GENERATION;
import static com.epickrram.tool.disruptor.GeneratorType.JDK_REFLECTION;

public final class PerfTest
{
    private static final int BUFFER_SIZE = 1024 * 512;
    private static final int INVOCATION_COUNT = 500000;
    private static final int RUNS = 20;
    private static final int EXPECTED_INVOCATION_COUNT = 2 * INVOCATION_COUNT;

    @Test
    public void comparePerformance() throws Exception
    {
        runTest(JDK_REFLECTION);
        runTest(BYTECODE_GENERATION);
    }

    private void runTest(final GeneratorType generatorType)
    {
        final CounterImpl counter = new CounterImpl(EXPECTED_INVOCATION_COUNT);
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final Disruptor<ProxyMethodInvocation> disruptor = new Disruptor<ProxyMethodInvocation>(RingBufferProxyEventFactory.FACTORY, executor,
                new SingleThreadedClaimStrategy(BUFFER_SIZE), new BusySpinWaitStrategy());

        final Counter proxy = getCounterProxy(generatorType, disruptor, counter);
        disruptor.start();

        performTestRuns(counter, proxy, generatorType.name());

        disruptor.shutdown();
        executor.shutdown();
    }

    private void performTestRuns(final CounterImpl counter, final Counter proxy, final String testName)
    {
        System.out.printf("Running test for proxy type: %s%n%n", testName);
        for(int i = 0; i < RUNS; i++)
        {
            performInvocations(counter, proxy, i, testName);
        }
        System.out.println("\n----------------------------------------\n\n");
    }

    private void performInvocations(final CounterImpl counter, final Counter proxy,
                                    final int run, final String testName)
    {
        final Object object = new Object();
        final long startNanos = System.nanoTime();
        for(int i = 0; i < INVOCATION_COUNT; i++)
        {
            proxy.doNothing();
            proxy.someArgs(17, "foo", Double.MAX_VALUE, object);
        }

        while(counter.getInvocationCountReachedTimestamp() == 0L)
        {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5L));
        }

        final long durationNanos = counter.getInvocationCountReachedTimestamp() - startNanos;

        final float opsPerSecond = EXPECTED_INVOCATION_COUNT / (durationNanos / (float)TimeUnit.SECONDS.toNanos(1L));
        final long avgLatency = durationNanos / EXPECTED_INVOCATION_COUNT;

        System.out.printf("[%s]/%d Ops per second: %.2f, avg latency %dns%n", testName, run, opsPerSecond, avgLatency);
        counter.reset();
    }

    private static Counter getCounterProxy(final GeneratorType generatorType,
                                           final Disruptor<ProxyMethodInvocation> disruptor,
                                           final CounterImpl implementation)
    {
        return new RingBufferProxyGeneratorFactory().create(generatorType).
                createRingBufferProxy(implementation, Counter.class, disruptor);
    }

    private static final class CounterImpl implements Counter
    {
        private final int expectedInvocationCount;
        private int invocationCount;
        private volatile long invocationCountReachedTimestamp;

        public CounterImpl(final int expectedInvocationCount)
        {
            this.expectedInvocationCount = expectedInvocationCount;
        }

        @Override
        public void doNothing()
        {
            if(++invocationCount == expectedInvocationCount)
            {
                invocationCountReachedTimestamp = System.nanoTime();
            }
        }

        @Override
        public void someArgs(final int one, final String two, final Double three, final Object four)
        {
            if(++invocationCount == expectedInvocationCount)
            {
                invocationCountReachedTimestamp = System.nanoTime();
            }
        }

        public long getInvocationCountReachedTimestamp()
        {
            return invocationCountReachedTimestamp;
        }

        void reset()
        {
            invocationCount = 0;
            invocationCountReachedTimestamp = 0L;
        }
    }

    public interface Counter
    {
        void doNothing();
        void someArgs(final int one, final String two, final Double three, final Object four);
    }
}
