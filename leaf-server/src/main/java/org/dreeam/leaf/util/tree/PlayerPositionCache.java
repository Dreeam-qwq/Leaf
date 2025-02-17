package org.dreeam.leaf.util.tree;

public class PlayerPositionCache {
    private static final int REGION_BITS = 3;
    static final double MOVEMENT_THRESHOLD = 4.0D;

    int chunkX;
    int chunkZ;
    long regionKey;
    int blockX;
    int blockZ;
    double lastX;
    double lastZ;

    public PlayerPositionCache(int chunkX, int chunkZ, double exactX, double exactZ) {
        this.lastX = exactX;
        this.lastZ = exactZ;
        this.update(chunkX, chunkZ, exactX, exactZ, false);
    }

    boolean shouldUpdate(double newX, double newZ) {
        double dx = newX - lastX;
        double dz = newZ - lastZ;
        return (dx * dx + dz * dz) >= (MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD);
    }

    void update(int newChunkX, int newChunkZ, double exactX, double exactZ, boolean force) {
        if (force || chunkX != newChunkX || chunkZ != newChunkZ) {
            chunkX = newChunkX;
            chunkZ = newChunkZ;
            blockX = newChunkX << 4;
            blockZ = newChunkZ << 4;
            regionKey = ((long)(chunkX >> REGION_BITS) & 0xFFFFFFFFL) | ((long)(chunkZ >> REGION_BITS) << 32);
            lastX = exactX;
            lastZ = exactZ;
        }
    }
}
