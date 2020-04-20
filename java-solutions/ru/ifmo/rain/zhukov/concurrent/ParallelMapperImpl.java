package ru.ifmo.rain.zhukov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.function.Function;

/**
 * Implementation of {@link ParallelMapper} interface using {@link QueueSolver}.
 */
public class ParallelMapperImpl implements ParallelMapper {
    QueueSolver queueSolver;

    /**
     * Creates parallel mapper with given number of threads.
     *
     * @param threads required number of threads.
     */
    public ParallelMapperImpl(int threads) {
        queueSolver = new QueueSolver(threads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ParallelCollector<R> collector = new ParallelCollector<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            int index = i;
            queueSolver.add(() -> collector.set(index, f.apply(args.get(index))));
        }
        return collector.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        queueSolver.close();
    }
}
