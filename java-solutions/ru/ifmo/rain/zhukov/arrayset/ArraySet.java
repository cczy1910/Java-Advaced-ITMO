package ru.ifmo.rain.zhukov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final ReversibleList<E> list;
    private final Comparator<? super E> comparator;

    private ArraySet(ReversibleList<E> reversibleList, Comparator<? super E> comparator) {
        this.list = reversibleList;
        this.comparator = comparator;
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        NavigableSet<E> tmp = new TreeSet<>(comparator);
        tmp.addAll(collection);
        list = new ReversibleList<>(tmp);
        this.comparator = Comparator.naturalOrder().equals(comparator) ? null : comparator;
    }

    private int index(E e, boolean inclusive, boolean lower) {
        int index = Collections.binarySearch(list, Objects.requireNonNull(e), comparator);
        if (index < 0) {
            return lower ? (-index - 1 - 1) : (-index - 1);
        } else {
            return inclusive ? index : (lower ? (index - 1) : (index + 1));
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (E) Objects.requireNonNull(o), comparator) >= 0;
    }

    @Override
    public E lower(E e) {
        return getIfExists(index(e, false, true));
    }

    @Override
    public E floor(E e) {
        return getIfExists(index(e, true, true));
    }

    @Override
    public E higher(E e) {
        return getIfExists(index(e, false, false));
    }

    @Override
    public E ceiling(E e) {
        return getIfExists(index(e, true, false));
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    private E getIfExists(int index) {
        return (0 <= index && index < size()) ? list.get(index) : null;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(list.reversed(), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    private NavigableSet<E> subSetImpl(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {

        int fromIndex = index(fromElement, fromInclusive, false);
        int toIndex = index(toElement, toInclusive, true);

        if (fromIndex > toIndex) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(list.subList(fromIndex, toIndex + 1), comparator);
    }

    @Override
    public NavigableSet<E> subSet(E e, boolean b, E e1, boolean b1) {
        if ((this.comparator == null ? ((Comparable<E>) e).compareTo(e1) : comparator.compare(e, e1)) > 0) {
            throw new IllegalArgumentException();
        }
        return subSetImpl(e, b, e1, b1);
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b) {
        if (isEmpty()) {
            return this;
        }
        return subSetImpl(first(), true, e, b);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b) {
        if (isEmpty()) {
            return this;
        }
        return subSetImpl(e, b, last(), true);
    }

    @Override
    public SortedSet<E> subSet(E e, E e1) {
        return subSet(e, true, e1, false);
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return headSet(e, false);
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return tailSet(e, true);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return list.get(0);
        }
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return list.get(size() - 1);
        }
    }

    @Override
    public int size() {
        return list.size();
    }
}
