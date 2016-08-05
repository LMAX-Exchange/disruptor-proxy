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

/**
 * Interface for receiving notifications pre and post message publication
 */
public interface MessagePublicationListener
{
    /**
     * Called before message is published to the ringbuffer
     */
    void onPrePublish();

    /**
     * Called after message is published to the ringbuffer. Will not be called if using DROP overflow strategy
     * and message is dropped.
     */
    void onPostPublish();
}
