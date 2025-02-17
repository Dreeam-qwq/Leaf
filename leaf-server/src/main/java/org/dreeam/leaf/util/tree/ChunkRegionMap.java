package org.dreeam.leaf.util.tree;

/**
 * Specialized map for chunk to region mapping using a fixed-size array with power-of-two based indexing.
 * This provides faster lookups than Long2LongOpenHashMap by eliminating hashing overhead and using
 * direct array indexing.
 */
public class ChunkRegionMap {
    private static final int BITS_PER_AXIS = 12; // 4096 chunks per axis
    private static final int SIZE_PER_AXIS = 1 << BITS_PER_AXIS;
    private static final int MASK = SIZE_PER_AXIS - 1;
    private static final int TOTAL_SIZE = SIZE_PER_AXIS * SIZE_PER_AXIS;

    private final long[] regions;
    private final boolean[] occupied;

    public ChunkRegionMap() {
        this.regions = new long[TOTAL_SIZE];
        this.occupied = new boolean[TOTAL_SIZE];
    }

    private int getIndex(int x, int z) {
        // convert to positive indices using modulo type shit
        x &= MASK;
        z &= MASK;
        return x + (z << BITS_PER_AXIS);
    }

    public void put(long chunkKey, long regionKey) {
        int x = (int) chunkKey;
        int z = (int) (chunkKey >>> 32);
        int index = getIndex(x, z);
        regions[index] = regionKey;
        occupied[index] = true;
    }

    public long get(long chunkKey) {
        int x = (int) chunkKey;
        int z = (int) (chunkKey >>> 32);
        int index = getIndex(x, z);
        return occupied[index] ? regions[index] : -1L;
    }

    public boolean containsKey(long chunkKey) {
        int x = (int) chunkKey;
        int z = (int) (chunkKey >>> 32);
        return occupied[getIndex(x, z)];
    }

    public void clear() {
        // only need to clear occupied flags, not the actual region values
        java.util.Arrays.fill(occupied, false);
    }
}
