package com.epickrram.tool.disruptor;

import com.lmax.disruptor.EventHandler;

public class ResetHandler implements EventHandler<ProxyMethodInvocation>
{

    @Override
    public void onEvent(ProxyMethodInvocation event, long arg1, boolean arg2) throws Exception
    {
        event.reset();
    }

}
