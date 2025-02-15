package org.dreeam.leaf.util.tree;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class SpatialPlayerIndex {
    private static final int REGION_BITS = 3;
    private static final double MAX_DISTANCE_SQ = 16384.0D; // Pre-computed square of 128

    // Using ObjectArrayList for better performance with small lists
    private final Long2ObjectOpenHashMap<ObjectArrayList<ServerPlayer>> regions = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<PlayerPositionCache> playerPositions = new Long2ObjectOpenHashMap<>();

    private static class PlayerPositionCache {
        int chunkX;
        int chunkZ;
        long regionKey;
        int blockX;
        int blockZ;
        double lastX;    // Last known exact coordinates
        double lastZ;
        static final double MOVEMENT_THRESHOLD = 4.0D;  // Only update if moved 4 blocks

        PlayerPositionCache(int chunkX, int chunkZ, double exactX, double exactZ) {
            this.lastX = exactX;
            this.lastZ = exactZ;
            this.update(chunkX, chunkZ, exactX, exactZ, false); // Force first update
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

    private static ObjectArrayList<ServerPlayer> getOrCreateList(Long2ObjectOpenHashMap<ObjectArrayList<ServerPlayer>> map, long key) {
        ObjectArrayList<ServerPlayer> list = map.get(key);
        if (list == null) {
            list = new ObjectArrayList<>(2);
            map.put(key, list);
        }
        return list;
    }

    private PlayerPositionCache getOrCreateCache(ServerPlayer player, int chunkX, int chunkZ) {
        PlayerPositionCache cache = playerPositions.get(player.getId());
        if (cache == null) {
            cache = new PlayerPositionCache(chunkX, chunkZ, player.getX(), player.getZ());
            playerPositions.put(player.getId(), cache);
        }
        return cache;
    }

    public void addPlayer(ServerPlayer player) {
        int chunkX = (int) player.getX() >> 4;
        int chunkZ = (int) player.getZ() >> 4;
        PlayerPositionCache cache = getOrCreateCache(player, chunkX, chunkZ);
        getOrCreateList(regions, cache.regionKey).add(player);
    }

    public void removePlayer(ServerPlayer player) {
        PlayerPositionCache cache = playerPositions.remove(player.getId());
        if (cache != null) {
            ObjectArrayList<ServerPlayer> players = regions.get(cache.regionKey);
            if (players != null) {
                players.remove(player);
                if (players.isEmpty()) {
                    regions.remove(cache.regionKey);
                }
            }
        }
    }

    public void updatePlayerPosition(ServerPlayer player) {
        double x = player.getX();
        double z = player.getZ();
        int newChunkX = (int) x >> 4;
        int newChunkZ = (int) z >> 4;

        PlayerPositionCache cache = playerPositions.get(player.getId());
        if (cache == null) {
            cache = new PlayerPositionCache(newChunkX, newChunkZ, x, z);
            playerPositions.put(player.getId(), cache);
            getOrCreateList(regions, cache.regionKey).add(player);
            return;
        }

        // Only update if moved enough or changed chunks
        if (cache.shouldUpdate(x, z) || cache.chunkX != newChunkX || cache.chunkZ != newChunkZ) {
            long oldKey = cache.regionKey;
            cache.update(newChunkX, newChunkZ, x, z, false);

            if (oldKey != cache.regionKey) {
                ObjectArrayList<ServerPlayer> oldList = regions.get(oldKey);
                if (oldList != null) {
                    oldList.remove(player);
                    if (oldList.isEmpty()) {
                        regions.remove(oldKey);
                    }
                }
                getOrCreateList(regions, cache.regionKey).add(player);
            }
        }
    }

    public boolean isChunkNearPlayer(ChunkMap chunkMap, ChunkPos chunkPos) {
        int regionX = chunkPos.x >> REGION_BITS;
        int regionZ = chunkPos.z >> REGION_BITS;

        long[] regionKeys = new long[9];

        long baseX = regionX - 1;
        for (int i = 0; i < 9; i++) {
            regionKeys[i] = ((baseX & 0xFFFFFFFFL) | ((long)(regionZ - 1 + (i / 3)) << 32));
            if ((i + 1) % 3 == 0) baseX++;
        }

        long centerKey = regionKeys[4];
        ObjectArrayList<ServerPlayer> centerPlayers = regions.get(centerKey);
        if (centerPlayers != null && checkPlayersFast(chunkPos, centerPlayers)) {
            return true;
        }

        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            ObjectArrayList<ServerPlayer> players = regions.get(regionKeys[i]);
            if (players != null && checkPlayersFast(chunkPos, players)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkPlayersFast(ChunkPos chunkPos, ObjectArrayList<ServerPlayer> players) {
        int size = players.size();
        if (size == 0) return false; // Early return for empty lists

        // Pre-calculate block coordinates once
        int blockX = chunkPos.x << 4;
        int blockZ = chunkPos.z << 4;
        blockX += 8; // Pre-add the 8 offset
        blockZ += 8;

        Object[] raw = players.elements();

        // Special case for small lists to avoid overhead
        if (size <= 2) {
            if (checkPlayerDistanceFast((ServerPlayer)raw[0], blockX, blockZ)) return true;
            return size == 2 && checkPlayerDistanceFast((ServerPlayer)raw[1], blockX, blockZ);
        }

        // Main loop handling 4 at a time
        int i = 0;
        int limit = size - 3; // Avoid recalculating each iteration
        while (i < limit) {
            if (checkPlayerDistanceFast((ServerPlayer)raw[i], blockX, blockZ) ||
                checkPlayerDistanceFast((ServerPlayer)raw[i + 1], blockX, blockZ) ||
                checkPlayerDistanceFast((ServerPlayer)raw[i + 2], blockX, blockZ) ||
                checkPlayerDistanceFast((ServerPlayer)raw[i + 3], blockX, blockZ)) {
                return true;
            }
            i += 4;
        }

        // Handle remaining players (0-3 players)
        while (i < size) {
            if (checkPlayerDistanceFast((ServerPlayer)raw[i], blockX, blockZ)) {
                return true;
            }
            i++;
        }

        return false;
    }

    private static boolean checkPlayerDistanceFast(ServerPlayer player, int blockX, int blockZ) {
        double dx = player.getX() - blockX;
        double dz = player.getZ() - blockZ;
        return (dx * dx + dz * dz) < MAX_DISTANCE_SQ;
    }

    public void forcePlayerUpdate(ServerPlayer player) { //TODO: add this as an option and enabled by default with the MOVEMENT_THRESHOLD in the config.
        double x = player.getX();
        double z = player.getZ();
        int newChunkX = (int) x >> 4;
        int newChunkZ = (int) z >> 4;

        PlayerPositionCache cache = playerPositions.get(player.getId());
        if (cache != null) {
            cache.update(newChunkX, newChunkZ, x, z, true);
        }
    }

    public void clear() {
        regions.clear();
        playerPositions.clear();
    }
}
