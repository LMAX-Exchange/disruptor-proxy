package com.epickrram.tool.disruptor;

public final class ProxyMethodInvocation
{
    private Invoker invoker;
    private Resetable argumentHolder;

    public Invoker getInvoker()
    {
        return invoker;
    }

    public void setInvoker(Invoker invoker)
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
