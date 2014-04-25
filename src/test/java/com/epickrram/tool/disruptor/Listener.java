package com.epickrram.tool.disruptor;

public interface Listener
{
    void onString(final String value);
    void onFloatAndInt(final Float value, final int intValue);
    void onVoid();
    void onObjectArray(final Double[] value);
    void onMixedMultipleArgs(final int int0, final int int1, final String s0, final String s1, final int i2);
}
