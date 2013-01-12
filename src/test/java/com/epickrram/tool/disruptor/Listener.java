package com.epickrram.tool.disruptor;

public interface Listener
{
    void onString(final String value);
    void onFloatAndInt(final Float value, final int intValue);
    void onVoid();
    void onObjectArray(final Double[] value);
}
