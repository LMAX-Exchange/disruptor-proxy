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
    public void invoke(Object implementation, Object[] args)
    {
        try
        {
            final int numberOfParameters = method.getParameterTypes().length;
            if(numberOfParameters == 0)
            {
                method.invoke(implementation, null);
            }
            else
            {
                final Object[] actualArgs = new Object[numberOfParameters];
                System.arraycopy(args, 0, actualArgs, 0, numberOfParameters);
                method.invoke(implementation, actualArgs);
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
