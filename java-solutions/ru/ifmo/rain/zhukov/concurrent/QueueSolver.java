package ru.ifmo.rain.zhukov.concurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Parallel solver for {@link ParallelMapperImpl}.
 * Queues given tasks ans solves them parallel.
 */
public class QueueSolver {
    /**
     * Task-solving thread.
     */
    private class Worker extends Thread {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    Runnable task;
                    synchronized (tasks) {
                        while (tasks.isEmpty()) {
                            tasks.wait();
                        }
                        task = tasks.poll();
                    }
                    task.run();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    private final List<Worker> workers = new ArrayList<>();
    private final Queue<Runnable> tasks = new LinkedList<>();

    /**
     * Creates solver with given number of threads.
     *
     * @param threads required number of threads.
     */
    public QueueSolver(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }
        for (int i = 0; i < threads; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            worker.start();
        }
    }

    /**
     * Enqueue task.
     *
     * @param task {@link Runnable} task to execute;
     */
    public void add(Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    /**
     * Interrupt and join all workers.
     */
    public void close() {
        for (Worker w : workers) {
            w.interrupt();
        }
        for (Worker w : workers) {
            try {
                w.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
