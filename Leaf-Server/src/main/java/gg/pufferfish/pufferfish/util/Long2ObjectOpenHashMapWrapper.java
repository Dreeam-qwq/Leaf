package gg.pufferfish.pufferfish.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class Long2ObjectOpenHashMapWrapper<V> extends Long2ObjectOpenHashMap<V> {

    private final Map<Long, V> backingMap;

    public Long2ObjectOpenHashMapWrapper(Map<Long, V> map) {
        backingMap = map;
    }

    @Override
    public V put(Long key, V value) {
        return backingMap.put(key, value);
    }

    @Override
    public V get(Object key) {
        return backingMap.get(key);
    }

    @Override
    public V remove(Object key) {
        return backingMap.remove(key);
    }

    @Nullable
    @Override
    public V putIfAbsent(Long key, V value) {
        return backingMap.putIfAbsent(key, value);
    }

    @Override
    public int size() {
        return backingMap.size();
    }
}
