/*
 * Copyright 2015-2016 LMAX Ltd.
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

package com.lmax.tool.disruptor.reflect;

import com.lmax.tool.disruptor.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ReflectiveMethodInvoker implements Invoker
{
    private final Method method;

    public ReflectiveMethodInvoker(final Method method)
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
                method.invoke(implementation, ((ObjectArrayHolder) argumentHolder).get());
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
