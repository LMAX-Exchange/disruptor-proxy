package com.epickrram.tool.disruptor.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.epickrram.tool.disruptor.Invoker;
import com.epickrram.tool.disruptor.InvokerEventHandler;
import com.epickrram.tool.disruptor.ProxyMethodInvocation;
import com.epickrram.tool.disruptor.RingBufferProxyGenerator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public final class ReflectiveRingBufferProxyGenerator implements RingBufferProxyGenerator
{
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(final T implementation, final Class<T> definition,
                                       final Disruptor<ProxyMethodInvocation> disruptor)
    {
        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(definition);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final RingBufferInvocationHandler<T> invocationHandler =
                new RingBufferInvocationHandler<T>(disruptor.getRingBuffer(), methodToInvokerMap);

        disruptor.handleEventsWith(new InvokerEventHandler<T>(implementation));

        return (T)Proxy.newProxyInstance(classLoader, new Class<?>[]{definition}, invocationHandler);
    }

    private static final class RingBufferInvocationHandler<T> implements InvocationHandler
    {
        private final RingBuffer<ProxyMethodInvocation> ringBuffer;
        private final Map<Method, Invoker> methodToInvokerMap;

        private RingBufferInvocationHandler(final RingBuffer<ProxyMethodInvocation> ringBuffer,
                                            final Map<Method, Invoker> methodToInvokerMap)
        {
            this.ringBuffer = ringBuffer;
            this.methodToInvokerMap = methodToInvokerMap;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
        {
            final long sequence = ringBuffer.next();
            try
            {
                final ProxyMethodInvocation proxyMethodInvocation = ringBuffer.getPreallocated(sequence);
                if(args != null)
                {
                    proxyMethodInvocation.ensureCapacity(args.length);
                    System.arraycopy(args, 0, proxyMethodInvocation.getArguments(), 0, args.length);
                }
                proxyMethodInvocation.setInvoker(methodToInvokerMap.get(method));
            }
            finally
            {
                ringBuffer.publish(sequence);
            }
            return null;
        }
    }

    private static <T> Map<Method, Invoker> createMethodToInvokerMap(Class<T> definition)
    {
        final Map<Method, Invoker> methodToInvokerMap = new ConcurrentHashMap<Method, Invoker>();

        final Method[] declaredMethods = definition.getDeclaredMethods();

        for (Method declaredMethod : declaredMethods)
        {
            methodToInvokerMap.put(declaredMethod, new ReflectiveMethodInvoker(declaredMethod));
        }

        return methodToInvokerMap;
    }
}