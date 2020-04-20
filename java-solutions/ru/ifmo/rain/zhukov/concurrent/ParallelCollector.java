package ru.ifmo.rain.zhukov.concurrent;

import java.util.ArrayList;
import java.util.List;


/**
 * Parallel result collector for {@link ParallelMapperImpl}
 * Allows to collect the result of mapping.
 */
public class ParallelCollector<R> {
    private final ArrayList<R> result;
    private int numCompleted = 0;


    /**
     * @param size size of list to collect.
     */
    public ParallelCollector(int size) {
        result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(null);
        }
    }

    /**
     * Set element at given index.
     */
    synchronized public void set(int index, R value) {
        result.set(index, value);
        numCompleted++;
        if (numCompleted == result.size()) {
            notifyAll();
        }
    }


    /**
     * Get the collected result.
     *
     * @return parallel collected result.
     */
    synchronized public List<R> get() throws InterruptedException {
        while (numCompleted < result.size()) {
            wait();
        }
        return result;
    }
}
