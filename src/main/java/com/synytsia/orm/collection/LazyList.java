package com.synytsia.orm.collection;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class LazyList<E> implements List<E> {

    private volatile List<E> delegate;

    private final Supplier<List<E>> supplier;

    public LazyList(Supplier<List<E>> supplier) {
        this.supplier = supplier;
    }

    private List<E> getDelegate() {
        if (delegate == null) {
            synchronized (supplier) {
                if (delegate == null) {
                    delegate = supplier.get();
                }
            }
        }
        return delegate;
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getDelegate().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return getDelegate().iterator();
    }

    @Override
    public Object[] toArray() {
        return getDelegate().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getDelegate().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return getDelegate().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return getDelegate().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getDelegate().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return getDelegate().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return getDelegate().addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getDelegate().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getDelegate().retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        getDelegate().replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        getDelegate().sort(c);
    }

    @Override
    public void clear() {
        getDelegate().clear();
    }

    @Override
    public boolean equals(Object o) {
        return getDelegate().equals(o);
    }

    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public E get(int index) {
        return getDelegate().get(index);
    }

    @Override
    public E set(int index, E element) {
        return getDelegate().set(index, element);
    }

    @Override
    public void add(int index, E element) {
        getDelegate().add(index, element);
    }

    @Override
    public E remove(int index) {
        return getDelegate().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return getDelegate().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getDelegate().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return getDelegate().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return getDelegate().listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return getDelegate().subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        return getDelegate().spliterator();
    }

    @Override
    public void addFirst(E e) {
        getDelegate().addFirst(e);
    }

    @Override
    public void addLast(E e) {
        getDelegate().addLast(e);
    }

    @Override
    public E getFirst() {
        return getDelegate().getFirst();
    }

    @Override
    public E getLast() {
        return getDelegate().getLast();
    }

    @Override
    public E removeFirst() {
        return getDelegate().removeFirst();
    }

    @Override
    public E removeLast() {
        return getDelegate().removeLast();
    }

    @Override
    public List<E> reversed() {
        return getDelegate().reversed();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return getDelegate().toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return getDelegate().removeIf(filter);
    }

    @Override
    public Stream<E> stream() {
        return getDelegate().stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return getDelegate().parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        getDelegate().forEach(action);
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }
}
