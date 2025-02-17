package org.dreeam.leaf.util.tree;

import org.dreeam.leaf.util.tree.SpatialPlayerIndex;

/**
 * Specialized array-based structure for storing player position caches.
 * Uses direct array access instead of hash map lookups for better performance.
 */
public class PlayerPositionArray {
    private static final int DEFAULT_SIZE = 512; // Default max players - will grow if needed
    private PlayerPositionCache[] caches;
    private int maxId;

    public PlayerPositionArray() {
        this.caches = new PlayerPositionCache[DEFAULT_SIZE];
        this.maxId = -1;
    }

    public void put(int playerId, PlayerPositionCache cache) {
        ensureCapacity(playerId);
        caches[playerId] = cache;
        if (playerId > maxId) {
            maxId = playerId;
        }
    }

    public PlayerPositionCache get(int playerId) {
        return playerId < caches.length ? caches[playerId] : null;
    }

    public PlayerPositionCache remove(int playerId) {
        if (playerId >= caches.length) return null;
        PlayerPositionCache old = caches[playerId];
        caches[playerId] = null;

        // Update maxId if needed
        if (playerId == maxId) {
            while (maxId >= 0 && caches[maxId] == null) {
                maxId--;
            }
        }

        return old;
    }

    public void clear() {
        // Only clear up to maxId to avoid unnecessary work
        for (int i = 0; i <= maxId; i++) {
            caches[i] = null;
        }
        maxId = -1;
    }

    private void ensureCapacity(int playerId) {
        if (playerId >= caches.length) {
            // Grow array to power of 2 size larger than required
            int newSize = Math.max(caches.length * 2, Integer.highestOneBit(playerId) << 1);
            PlayerPositionCache[] newCaches = new PlayerPositionCache[newSize];
            System.arraycopy(caches, 0, newCaches, 0, caches.length);
            caches = newCaches;
        }
    }
}
