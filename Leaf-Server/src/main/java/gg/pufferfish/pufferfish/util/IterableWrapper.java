package gg.pufferfish.pufferfish.util;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

public class IterableWrapper<T> implements Iterable<T> {

    private final Iterator<T> iterator;

    public IterableWrapper(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return iterator;
    }
}
