/*
 * Copyright 2015 LMAX Ltd.
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
 * An interface to describe objects that will be called on the end of a Disruptor batch.
 *
 * Implement this interface in your implementation object if you wish to be notified of the end of a Disruptor batch.
 */
public interface BatchListener
{
    /**
     * Will be called at the end of a Disruptor batch
     */
    void onEndOfBatch();
}
