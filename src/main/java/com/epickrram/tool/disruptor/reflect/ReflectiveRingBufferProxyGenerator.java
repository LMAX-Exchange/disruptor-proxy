package com.epickrram.tool.disruptor.reflect;

import com.epickrram.tool.disruptor.*;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectiveRingBufferProxyGenerator implements RingBufferProxyGenerator
{
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(final T implementation, final Class<T> definition,
                                       final Disruptor<ProxyMethodInvocation> disruptor,
                                       final OverflowStrategy overflowStrategy)
    {
        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(definition);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final RingBufferInvocationHandler<T> invocationHandler =
                new RingBufferInvocationHandler<T>(disruptor.getRingBuffer(), methodToInvokerMap, overflowStrategy);

        preallocateArgumentHolders(disruptor.getRingBuffer());

        disruptor.handleEventsWith(new InvokerEventHandler<T>(implementation));

        return (T)Proxy.newProxyInstance(classLoader, new Class<?>[]{definition}, invocationHandler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(Class<T> definition, Disruptor<ProxyMethodInvocation> disruptor,
                                       OverflowStrategy overflowStrategy, T... implementations)
    {
        InvokerEventHandler<T>[] handlers = new InvokerEventHandler[implementations.length];
        for (int i = 0; i < implementations.length; i++)
        {
            handlers[i] = new InvokerEventHandler<T>(implementations[i], false);
            disruptor.handleEventsWith(handlers[i]);
        }
        disruptor.after(handlers).then(new ResetHandler());

        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(definition);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final RingBufferInvocationHandler<T> invocationHandler =
                new RingBufferInvocationHandler<T>(disruptor.getRingBuffer(), methodToInvokerMap, overflowStrategy);

        preallocateArgumentHolders(disruptor.getRingBuffer());

        return (T)Proxy.newProxyInstance(classLoader, new Class<?>[]{definition}, invocationHandler);
    }

    private void preallocateArgumentHolders(final RingBuffer<ProxyMethodInvocation> ringBuffer)
    {
        final int bufferSize = ringBuffer.getBufferSize();
        for(int i = 0; i < bufferSize; i++)
        {
            ringBuffer.get(i).setArgumentHolder(new ObjectArrayHolder());
        }
    }

    private static final class RingBufferInvocationHandler<T> implements InvocationHandler
    {
        private final RingBuffer<ProxyMethodInvocation> ringBuffer;
        private final Map<Method, Invoker> methodToInvokerMap;
        private final OverflowStrategy overflowStrategy;

        private RingBufferInvocationHandler(final RingBuffer<ProxyMethodInvocation> ringBuffer,
                                            final Map<Method, Invoker> methodToInvokerMap,
                                            final OverflowStrategy overflowStrategy)
        {
            this.ringBuffer = ringBuffer;
            this.methodToInvokerMap = methodToInvokerMap;
            this.overflowStrategy = overflowStrategy;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
        {
            if(overflowStrategy == OverflowStrategy.DROP && !ringBuffer.hasAvailableCapacity(1))
            {
                return null;
            }
            final long sequence = ringBuffer.next();
            try
            {
                final ProxyMethodInvocation proxyMethodInvocation = ringBuffer.get(sequence);
                final ObjectArrayHolder argumentHolder = (ObjectArrayHolder) proxyMethodInvocation.getArgumentHolder();
                if(args != null)
                {
                    final Object[] copyOfArgs = new Object[args.length];
                    System.arraycopy(args, 0, copyOfArgs, 0, args.length);
                    argumentHolder.set(copyOfArgs);
                }
                else
                {
                    argumentHolder.set(null);
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