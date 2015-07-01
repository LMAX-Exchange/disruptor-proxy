package com.lmax.tool.disruptor;

public class ValidationConfig
{
    private final boolean validateProxyInterfaces;
    private final boolean validateExceptionHandler;

    public ValidationConfig(boolean validateProxyInterfaces, boolean validateExceptionHandler)
    {
        this.validateProxyInterfaces = validateProxyInterfaces;
        this.validateExceptionHandler = validateExceptionHandler;
    }

    public boolean validateProxyInterfaces()
    {
        return validateProxyInterfaces;
    }

    public boolean validateExceptionHandler()
    {
        return validateExceptionHandler;
    }
}
