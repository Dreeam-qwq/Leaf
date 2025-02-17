package org.dreeam.leaf.util.tree;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class SpatialPlayerIndex {
    private static final int REGION_BITS = 3;
    private static final double MAX_DISTANCE_SQ = 16384.0D;

    private final Long2ObjectOpenHashMap<RegionPlayerList> regions = new Long2ObjectOpenHashMap<>();
    private final PlayerPositionArray playerPositions = new PlayerPositionArray();
    private final ChunkRegionMap chunkToRegion = new ChunkRegionMap();

    private static RegionPlayerList getOrCreateList(Long2ObjectOpenHashMap<RegionPlayerList> map, long key) {
        RegionPlayerList list = map.get(key);
        if (list == null) {
            list = new RegionPlayerList();
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
            RegionPlayerList players = regions.get(cache.regionKey);
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
                RegionPlayerList oldList = regions.get(oldKey);
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
        long chunkKey = ((long)chunkPos.x & 0xFFFFFFFFL) | ((long)chunkPos.z << 32);

        // If we don't have this chunk mapped, compute its region and surrounding regions
        if (!chunkToRegion.containsKey(chunkKey)) {
            updateChunkToRegionMapping(chunkPos.x, chunkPos.z);
        }

        long regionKey = chunkToRegion.get(chunkKey);
        if (regionKey == -1L) {
            return false;
        }

        RegionPlayerList players = regions.get(regionKey);
        if (players != null && checkPlayersFast(chunkPos, players)) {
            return true;
        }

        // Check surrounding regions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                int nearbyRegionX = (chunkPos.x >> REGION_BITS) + dx;
                int nearbyRegionZ = (chunkPos.z >> REGION_BITS) + dz;
                long nearbyRegionKey = ((long)(nearbyRegionX) & 0xFFFFFFFFL) | ((long)(nearbyRegionZ) << 32);

                players = regions.get(nearbyRegionKey);
                if (players != null && checkPlayersFast(chunkPos, players)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkPlayersFast(ChunkPos chunkPos, RegionPlayerList players) {
        if (players.isEmpty()) return false;

        // Pre-calculate block coordinates once
        int blockX = chunkPos.x << 4;
        int blockZ = chunkPos.z << 4;
        blockX += 8;
        blockZ += 8;

        ServerPlayer[] rawPlayers = players.getPlayers();
        int size = players.size();

        // Special case for small lists to avoid overhead
        if (size <= 2) {
            if (checkPlayerDistanceFast(rawPlayers[0], blockX, blockZ)) return true;
            return size == 2 && checkPlayerDistanceFast(rawPlayers[1], blockX, blockZ);
        }

        // Main loop handling 4 at a time
        int i = 0;
        int limit = size - 3;
        while (i < limit) {
            if (checkPlayerDistanceFast(rawPlayers[i], blockX, blockZ) ||
                checkPlayerDistanceFast(rawPlayers[i + 1], blockX, blockZ) ||
                checkPlayerDistanceFast(rawPlayers[i + 2], blockX, blockZ) ||
                checkPlayerDistanceFast(rawPlayers[i + 3], blockX, blockZ)) {
                return true;
            }
            i += 4;
        }

        // Handle remaining players
        while (i < size) {
            if (checkPlayerDistanceFast(rawPlayers[i], blockX, blockZ)) {
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

    private void updateChunkToRegionMapping(int chunkX, int chunkZ) {
        long chunkKey = ((long)chunkX & 0xFFFFFFFFL) | ((long)chunkZ << 32);
        int regionX = chunkX >> REGION_BITS;
        int regionZ = chunkZ >> REGION_BITS;
        long regionKey = ((long)(regionX) & 0xFFFFFFFFL) | ((long)(regionZ) << 32);
        chunkToRegion.put(chunkKey, regionKey);

        // Add surrounding chunks in the same region
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int nearbyChunkX = chunkX + dx;
                int nearbyChunkZ = chunkZ + dz;
                int nearbyRegionX = nearbyChunkX >> REGION_BITS;
                int nearbyRegionZ = nearbyChunkZ >> REGION_BITS;
                long nearbyChunkKey = ((long)nearbyChunkX & 0xFFFFFFFFL) | ((long)nearbyChunkZ << 32);
                long nearbyRegionKey = ((long)(nearbyRegionX) & 0xFFFFFFFFL) | ((long)(nearbyRegionZ) << 32);
                chunkToRegion.put(nearbyChunkKey, nearbyRegionKey);
            }
        }
    }

    public void forcePlayerUpdate(ServerPlayer player) {
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
        chunkToRegion.clear();
    }
}
