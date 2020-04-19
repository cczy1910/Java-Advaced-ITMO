package ru.ifmo.rain.zhukov.arrayset;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

public class ReversibleList<E> extends AbstractList<E> implements RandomAccess {
    private final List<E> list;
    private final boolean reversed;

    private ReversibleList(List<E> list, boolean reversed) {
        this.list = list;
        this.reversed = reversed;
    }

    ReversibleList(Collection<? extends E> collection) {
        list = List.copyOf(collection);
        reversed = false;
    }


    private int reverseIndex(int index) {
        return size() - 1 - index;
    }


    @Override
    public E get(int i) {
        return list.get(reversed ? reverseIndex(i) : i);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public ReversibleList<E> subList(int fromIndex, int toIndex) {
        if (reversed) {
            return new ReversibleList<>(list.subList(reverseIndex(toIndex - 1), reverseIndex(fromIndex) + 1), true);
        } else {
            return new ReversibleList<>(list.subList(fromIndex, toIndex), false);
        }
    }

    public ReversibleList<E> reversed() {
        return new ReversibleList<>(list, !reversed);
    }
}
