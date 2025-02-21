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
public final class ConcurrentLongHashSet extends LongOpenHashSet implements LongSet {
    private final ConcurrentHashMap<Long, Boolean> backingMap;

    /**
     * Creates a new empty concurrent long set.
     */
    public ConcurrentLongHashSet() {
        // Some tips to minimize tree bin conversions:
        // - Higher initial capacity (64)
        // - Lower load factor (0.5)
        // - Concurrency level = number of threads
        int concurrencyLevel = Runtime.getRuntime().availableProcessors();
        this.backingMap = new ConcurrentHashMap<>(64, 0.5f, concurrencyLevel);
    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public @NotNull LongIterator iterator() {
        return new WrappingLongIterator(backingMap.keySet().iterator());
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return backingMap.keySet().toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] array) {
        Objects.requireNonNull(array, "Array cannot be null");
        return backingMap.keySet().toArray(array);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backingMap.keySet().containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Long> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backingMap.keySet().addAll(collection);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backingMap.keySet().removeAll(collection);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        Objects.requireNonNull(collection, "Collection cannot be null");
        return backingMap.keySet().retainAll(collection);
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public boolean add(long key) {
        // Use putIfAbsent to avoid duplicate entries
        return backingMap.putIfAbsent(key, Boolean.TRUE) == null;
    }

    @Override
    public boolean contains(long key) {
        // Autoboxing cache for common values (-128 to 127)
        if (key >= -128 && key <= 127) {
            return backingMap.containsKey((long) key);
        }
        return backingMap.containsKey(key);
    }

    @Override
    public long[] toLongArray() {
        int size = backingMap.size();
        long[] result = new long[size];
        int i = 0;
        for (Long value : backingMap.keySet()) {
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
        return backingMap.keySet().retainAll(c);
    }

    @Override
    public boolean remove(long key) {
        return backingMap.remove(key) != null;
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
        return backingMap.hashCode();
    }

    @Override
    public String toString() {
        return backingMap.toString();
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
            return backing.next(); //TODO: Autoboxing still occurs here
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
