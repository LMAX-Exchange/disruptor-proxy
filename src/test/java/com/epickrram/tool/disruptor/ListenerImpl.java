package com.epickrram.tool.disruptor;

public final class ListenerImpl implements Listener
{
    private String lastStringValue;
    private int lastIntValue;
    private Float lastFloatValue;
    private int voidInvocationCount = 0;
    private Double[] lastDoubleArray;

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
}
