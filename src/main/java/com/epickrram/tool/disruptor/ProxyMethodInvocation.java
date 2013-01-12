package com.epickrram.tool.disruptor;

public final class ProxyMethodInvocation
{
    public static final int INITIAL_ARGUMENT_LENGTH = 1;

    private Invoker invoker;
    private Object[] arguments = new Object[INITIAL_ARGUMENT_LENGTH];
    private int argumentsLength = INITIAL_ARGUMENT_LENGTH;

    public Invoker getInvoker()
    {
        return invoker;
    }

    public void setInvoker(Invoker invoker)
    {
        this.invoker = invoker;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public void ensureCapacity(final int numberOfArguments)
    {
        if(numberOfArguments > argumentsLength)
        {
            arguments = new Object[numberOfArguments];
            argumentsLength = numberOfArguments;
        }
    }

    public void reset()
    {
        invoker = null;
        for(int i = 0; i < argumentsLength; i++)
        {
            arguments[i++] = null;
        }
    }
}
