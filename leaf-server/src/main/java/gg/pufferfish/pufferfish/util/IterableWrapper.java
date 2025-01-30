package gg.pufferfish.pufferfish.util;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

public record IterableWrapper<T>(Iterator<T> iterator) implements Iterable<T> {
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return iterator;
    }
}
