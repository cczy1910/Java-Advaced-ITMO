package ru.ifmo.rain.zhukov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ListIP} and {@link ScalarIP} interfaces using iterative parallelism.
 */
public class IterativeParallelism implements ListIP {
    /**
     * Collects values by collector.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to reduce.
     * @param collector collector.
     * @param combiner  combiner for partial results.
     * @return result of values collection.
     * @throws InterruptedException if executing thread was interrupted.
     */
    private <T, R> R collect(int threads, List<? extends T> values,
                             Function<Stream<? extends T>, ? extends R> collector, Function<Stream<? extends R>, ? extends R> combiner) throws InterruptedException {
        List<Thread> jobs = new ArrayList<>();
        List<List<? extends T>> partition = createPartition(threads, values);
        List<R> parallelResults = new ArrayList<>(Collections.nCopies(partition.size(), null));
        InterruptedException exception = new InterruptedException("Thread interruption error");
        for (int i = 0; i < partition.size(); i++) {
            int thread = i;
            jobs.add(new Thread(() -> parallelResults.set(thread, collector.apply(partition.get(thread).stream()))));
            jobs.get(jobs.size() - 1).start();
        }
        for (Thread job : jobs) {
            try {
                job.join();
            } catch (InterruptedException e) {
                exception.addSuppressed(e);
            }
        }
        if (exception.getSuppressed().length != 0) {
            throw exception;
        }
        return combiner.apply(parallelResults.stream());
    }

    /**
     * Reduces values by function.
     *
     * @param threads number of concurrent threads.
     * @param values  values to reduce.
     * @param f       reduce function.
     * @return result of values reduction.
     * @throws InterruptedException if executing thread was interrupted.
     */
    private <T> T reduce(int threads, List<? extends T> values,
                         Function<Stream<? extends T>, T> f) throws InterruptedException {
        return collect(threads, values, f, f);
    }

    /**
     * Transforms list of values by transformation.
     *
     * @param threads        number of concurrent threads.
     * @param values         values to filter.
     * @param transformation lists streams transformation.
     * @return transformed list of values
     * @throws InterruptedException if executing thread was interrupted.
     */
    private <T, R> List<R> transform(int threads, List<? extends T> values,
                                     Function<Stream<? extends T>, Stream<? extends R>> transformation) throws InterruptedException {
        return collect(threads, values,
                transformation,
                stream -> stream.flatMap(Function.identity())).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return transform(threads, values,
                stream -> stream.filter(predicate));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return transform(threads, values,
                stream -> stream.map(f));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return collect(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return reduce(threads, values, stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return reduce(threads, values, stream -> stream.min(comparator).orElseThrow());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return collect(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return collect(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }

    /**
     * Splits list of values fo given number of threads.
     *
     * @param threads number of concurrent threads.
     * @param values  list of values to split
     * @return partition for given number of threads.
     */
    private <T> List<List<? extends T>> createPartition(int threads, List<? extends T> values) {
        int[] borders = new int[threads + 1];
        int partSize = values.size() / threads;
        int restNum = values.size() % threads;
        for (int i = 0; i <= threads; i++) {
            borders[i] = i * partSize + Math.min(i, restNum);
        }
        List<List<? extends T>> result = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            if (borders[i + 1] - borders[i] != 0) {
                result.add(values.subList(borders[i], borders[i + 1]));
            } else {
                break;
            }
        }
        return result;
    }
}
