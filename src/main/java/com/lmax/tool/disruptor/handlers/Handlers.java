package com.lmax.tool.disruptor.handlers;

import com.lmax.disruptor.EventHandler;
import com.lmax.tool.disruptor.BatchListener;
import com.lmax.tool.disruptor.BatchSizeListener;
import com.lmax.tool.disruptor.ProxyMethodInvocation;

public class Handlers
{
    public static <T> EventHandler<ProxyMethodInvocation> createSingleImplementationHandlerChain(final T implementation)
    {
        return createHandlers(implementation, true);
    }

    public static <T> EventHandler<ProxyMethodInvocation> createMultipleImplementationHandlerChain(final T implementation)
    {
        return createHandlers(implementation, false);
    }

    private static <T> EventHandler<ProxyMethodInvocation> createHandlers(final T implementation, final boolean reset)
    {
        EventHandler<ProxyMethodInvocation> handler = new InvokerEventHandler<T>(implementation, reset);
        if (implementation instanceof BatchListener)
        {
            handler = new EndOfBatchEventHandler((BatchListener) implementation, handler);
        }
        if (implementation instanceof BatchSizeListener)
        {
            handler = new BatchSizeReportingEventHandler((BatchSizeListener) implementation, handler);
        }
        return handler;
    }
}
