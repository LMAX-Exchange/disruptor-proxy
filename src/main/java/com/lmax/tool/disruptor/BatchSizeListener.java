package com.lmax.tool.disruptor;

/**
 * An interface to describe objects that will be notified of the current batch size at the end of each Disruptor batch.
 *
 * Implement this interface in your implementation object if you wish to be notified of the size of each Disruptor batch.
 */
public interface BatchSizeListener
{

    /**
     * Will be called at the end of a Disruptor batch
     * @param batchSize  The size of the batch
     */
    void onEndOfBatch(int batchSize);

}
