package com.epickrram.tool.disruptor.reflect;

import com.epickrram.tool.disruptor.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ReflectiveMethodInvoker implements Invoker
{
    private final Method method;

    public ReflectiveMethodInvoker(Method method)
    {
        this.method = method;
    }

    @Override
    public void invokeWithArgumentHolder(final Object implementation, final Object argumentHolder)
    {
        try
        {
            final int numberOfParameters = method.getParameterTypes().length;
            if(numberOfParameters == 0)
            {
                method.invoke(implementation, (Object[]) null);
            }
            else
            {
                method.invoke(implementation, (Object[]) argumentHolder);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Failed to invoke", e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException("Failed to invoke", e);
        }
    }
}
