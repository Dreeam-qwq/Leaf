package org.dreeam.leaf.util.cache;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.BitSet;
import java.util.function.IntFunction;

public class CachedOrNewBitsGetter {

    private static final IntFunction<BitSet> bitSetConstructor = BitSet::new;

    public static ThreadLocal<Int2ObjectOpenHashMap<BitSet>> BITSETS = ThreadLocal.withInitial(Int2ObjectOpenHashMap::new);

    private CachedOrNewBitsGetter() {
    }

    public static BitSet getCachedOrNewBitSet(int bits) {
        final BitSet bitSet = BITSETS.get().computeIfAbsent(bits, bitSetConstructor);
        bitSet.clear();
        return bitSet;
    }
}
