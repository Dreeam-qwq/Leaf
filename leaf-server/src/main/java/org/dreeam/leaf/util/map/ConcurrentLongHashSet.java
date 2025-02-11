package org.dreeam.leaf.util.map;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe implementation of {@link LongOpenHashSet} using ConcurrentHashMap.KeySetView as backing storage.
 * This implementation provides concurrent access and high performance for concurrent operations.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class ConcurrentLongHashSet extends LongOpenHashSet implements LongSet { // Extending LongOpenHashSet for some moonrise usages
    private final ConcurrentHashMap.KeySetView<Long, Boolean> backing;

    /**
     * Creates a new empty concurrent long set.
     */
    public ConcurrentLongHashSet() {
        this.backing = ConcurrentHashMap.newKeySet();
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public boolean isEmpty() {
        return backing.isEmpty();
    }

    @Override
    public @NotNull LongIterator iterator() {
        return new WrappingLongIterator(backing.iterator());
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return backing.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] array) {
        Objects.requireNonNull(array, "Array cannot be null");
        return backing.toArray(array);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backing.containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Long> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backing.addAll(collection);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backing.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backing.retainAll(collection);
    }

    @Override
    public void clear() {
        backing.clear();
    }

    @Override
    public boolean add(long key) {
        return backing.add(key);
    }

    @Override
    public boolean contains(long key) {
        return backing.contains(key);
    }

    @Override
    public long[] toLongArray() {
        int size = backing.size();
        long[] result = new long[size];
        int i = 0;
        for (Long value : backing) {
            result[i++] = value;
        }
        return result;
    }

    @Override
    public long[] toArray(long[] array) {
        Objects.requireNonNull(array, "Array cannot be null");
        long[] result = toLongArray();
        if (array.length < result.length) {
            return result;
        }
        System.arraycopy(result, 0, array, 0, result.length);
        if (array.length > result.length) {
            array[result.length] = 0;
        }
        return array;
    }

    @Override
    public boolean addAll(LongCollection c) {
        Objects.requireNonNull(c, "Collection cannot be null");
        boolean modified = false;
        LongIterator iterator = c.iterator();
        while (iterator.hasNext()) {
            modified |= add(iterator.nextLong());
        }
        return modified;
    }

    @Override
    public boolean containsAll(LongCollection c) {
        Objects.requireNonNull(c, "Collection cannot be null");
        LongIterator iterator = c.iterator();
        while (iterator.hasNext()) {
            if (!contains(iterator.nextLong())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(LongCollection c) {
        Objects.requireNonNull(c, "Collection cannot be null");
        boolean modified = false;
        LongIterator iterator = c.iterator();
        while (iterator.hasNext()) {
            modified |= remove(iterator.nextLong());
        }
        return modified;
    }

    @Override
    public boolean retainAll(LongCollection c) {
        Objects.requireNonNull(c, "Collection cannot be null");
        return backing.retainAll(c);
    }

    @Override
    public boolean remove(long k) {
        return backing.remove(k);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongSet that)) return false;
        if (size() != that.size()) return false;
        return containsAll(that);
    }

    @Override
    public int hashCode() {
        return backing.hashCode();
    }

    @Override
    public String toString() {
        return backing.toString();
    }

    static class WrappingLongIterator implements LongIterator {
        private final Iterator<Long> backing;

        WrappingLongIterator(Iterator<Long> backing) {
            this.backing = Objects.requireNonNull(backing);
        }

        @Override
        public boolean hasNext() {
            return backing.hasNext();
        }

        @Override
        public long nextLong() {
            return backing.next();
        }

        @Override
        public Long next() {
            return backing.next();
        }

        @Override
        public void remove() {
            backing.remove();
        }
    }
}
