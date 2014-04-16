package com.epickrram.tool.disruptor;

public final class ProxyMethodInvocation
{
    public static final int INITIAL_ARGUMENT_LENGTH = 1;
    private static final Object EMPTY_ARGUMENTS_MARKER = new Object();

    private Invoker invoker;
    private Object[] arguments = new Object[INITIAL_ARGUMENT_LENGTH];
    private int argumentsLength = INITIAL_ARGUMENT_LENGTH;
    private Object argumentHolder;

    public Invoker getInvoker()
    {
        return invoker;
    }

    public void setInvoker(Invoker invoker)
    {
        this.invoker = invoker;
    }

    public void setArgumentHolder(final Object argumentHolder)
    {
        this.argumentHolder = argumentHolder;
    }

    public void emptyArguments()
    {
        argumentHolder = EMPTY_ARGUMENTS_MARKER;
    }

    public boolean hasArguments()
    {
        return argumentHolder == null || argumentHolder == EMPTY_ARGUMENTS_MARKER;
    }

    public Object getArgumentHolder()
    {
        return argumentHolder;
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
        argumentHolder = null;
        for(int i = 0; i < argumentsLength; i++)
        {
            arguments[i++] = null;
        }
    }
}
