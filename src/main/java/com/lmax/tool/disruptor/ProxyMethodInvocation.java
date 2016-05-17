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

package com.lmax.tool.disruptor;

/**
 * A container for storing the arguments of a method invocation
 */
public final class ProxyMethodInvocation
{
    private Invoker invoker;
    private Resetable argumentHolder;

    public Invoker getInvoker()
    {
        return invoker;
    }

    public void setInvoker(final Invoker invoker)
    {
        this.invoker = invoker;
    }

    public void setArgumentHolder(final Resetable argumentHolder)
    {
        this.argumentHolder = argumentHolder;
    }

    public Object getArgumentHolder()
    {
        return argumentHolder;
    }

    public void reset()
    {
        argumentHolder.reset();
    }
}
