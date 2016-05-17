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

public final class ListenerImpl implements Listener
{
    private volatile String lastStringValue;
    private volatile int lastIntValue;
    private volatile Float lastFloatValue;
    private volatile int voidInvocationCount;
    private volatile Double[] lastDoubleArray;
    private int mixedArgsInvocationCount;

    @Override
    public void onString(final String value)
    {
        lastStringValue = value;
    }

    @Override
    public void onFloatAndInt(final Float floatValue, final int intValue)
    {
        lastFloatValue = floatValue;
        lastIntValue = intValue;
    }

    @Override
    public void onVoid()
    {
        voidInvocationCount++;
    }

    @Override
    public void onObjectArray(final Double[] value)
    {
        lastDoubleArray = value;
    }

    @Override
    public void onMixedMultipleArgs(final int int0, final int int1, final String s0, final String s1, final int i2)
    {
        mixedArgsInvocationCount++;
    }

    public String getLastStringValue()
    {
        return lastStringValue;
    }

    public int getLastIntValue()
    {
        return lastIntValue;
    }

    public Float getLastFloatValue()
    {
        return lastFloatValue;
    }

    public int getVoidInvocationCount()
    {
        return voidInvocationCount;
    }

    public Double[] getLastDoubleArray()
    {
        return lastDoubleArray;
    }

    public int getMixedArgsInvocationCount()
    {
        return mixedArgsInvocationCount;
    }
}
