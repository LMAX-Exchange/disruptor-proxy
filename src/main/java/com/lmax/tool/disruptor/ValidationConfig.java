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

public interface ValidationConfig
{
    enum ProxyInterface
    {
        REQUIRES_ANNOTATION(true), NO_ANNOTATION(false);

        private final boolean validateProxyInterfaces;

        ProxyInterface(final boolean validateProxyInterfaces)
        {
            this.validateProxyInterfaces = validateProxyInterfaces;
        }

        public boolean shouldValidateProxyInterfaces()
        {
            return validateProxyInterfaces;
        }
    }

    enum ExceptionHandler
    {
        REQUIRED(true), NOT_REQUIRED(false);

        private final boolean validateExceptionHandler;

        ExceptionHandler(final boolean validateExceptionHandler)
        {
            this.validateExceptionHandler = validateExceptionHandler;
        }

        public boolean shouldValidateExceptionHandler()
        {
            return validateExceptionHandler;
        }
    }

    boolean validateProxyInterfaces();

    boolean validateExceptionHandler();
}
