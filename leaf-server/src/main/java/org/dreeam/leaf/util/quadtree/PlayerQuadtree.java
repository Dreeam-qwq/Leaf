package org.dreeam.leaf.util.quadtree;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class PlayerQuadtree {
    private static final int MAX_OBJECTS = 4;
    private static final int MAX_LEVELS = 5;
    private static final int INITIAL_CAPACITY = 64;

    private final int level;
    private final Rectangle2D bounds;
    private ServerPlayer[] players;
    private int playerCount;
    private PlayerQuadtree[] nodes;

    // Cached bounds values to avoid repeated calculations
    private final int minX, maxX, minZ, maxZ, midX, midZ;

    // Pre-allocated work queue for non-recursive retrieval
    private static final PlayerQuadtree[] WORK_QUEUE = new PlayerQuadtree[32];
    private static int queueSize;

    public PlayerQuadtree(int level, Rectangle2D bounds) {
        this.level = level;
        this.bounds = bounds;
        this.players = new ServerPlayer[INITIAL_CAPACITY];
        this.playerCount = 0;

        // Cache all boundary calculations
        this.minX = bounds.x;
        this.maxX = bounds.x + bounds.width;
        this.minZ = bounds.z;
        this.maxZ = bounds.z + bounds.height;
        this.midX = bounds.x + (bounds.width >> 1);
        this.midZ = bounds.z + (bounds.height >> 1);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > players.length) {
            int newCapacity = Math.max(players.length << 1, minCapacity);
            players = Arrays.copyOf(players, newCapacity);
        }
    }

    /** Splits the node into 4 subnodes */
    private void split() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int z = bounds.z;

        nodes = new PlayerQuadtree[4];
        nodes[0] = new PlayerQuadtree(level + 1, new Rectangle2D(x, z, subWidth, subHeight));          // Top-left
        nodes[1] = new PlayerQuadtree(level + 1, new Rectangle2D(x + subWidth, z, subWidth, subHeight)); // Top-right
        nodes[2] = new PlayerQuadtree(level + 1, new Rectangle2D(x, z + subHeight, subWidth, subHeight)); // Bottom-left
        nodes[3] = new PlayerQuadtree(level + 1, new Rectangle2D(x + subWidth, z + subHeight, subWidth, subHeight)); // Bottom-right
    }

    /**
     * Determines which node the player belongs to.
     * Returns -1 if the player doesn't fit entirely within a child node.
     */
    private int getIndex(ServerPlayer player) {
        double midX = bounds.x + bounds.width / 2.0;
        double midZ = bounds.z + bounds.height / 2.0;
        double playerX = player.getX();
        double playerZ = player.getZ();

        boolean topQuadrant = (playerZ < midZ);
        boolean bottomQuadrant = (playerZ >= midZ);

        if (playerX < midX) {
            if (topQuadrant) return 0; // Top-left
            if (bottomQuadrant) return 2; // Bottom-left
        } else {
            if (topQuadrant) return 1; // Top-right
            if (bottomQuadrant) return 3; // Bottom-right
        }
        return -1; // Doesn't fully fit in a quadrant
    }

    /** Inserts a player into the quadtree */

    public void insert(ServerPlayer player) {
        if (nodes != null) {
            int index = getIndex(player);
            if (index != -1) {
                nodes[index].insert(player);
                return;
            }
        }

        ensureCapacity(playerCount + 1);
        players[playerCount++] = player;

        if (playerCount > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes == null) {
                split();
            }

            int i = 0;
            while (i < playerCount) {
                ServerPlayer p = players[i];
                int index = getIndex(p);
                if (index != -1) {
                    nodes[index].insert(p);
                    // Remove by replacing with last element
                    players[i] = players[--playerCount];
                    players[playerCount] = null;
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Retrieves players that might be within a given area.
     * Uses a preallocated list (`returnObjects`) to avoid excess memory allocations.
     */
    public List<ServerPlayer> retrieve(List<ServerPlayer> results, Rectangle2D area) {
        queueSize = 0;
        WORK_QUEUE[queueSize++] = this;

        final int areaMinX = area.x;
        final int areaMaxX = area.x + area.width;
        final int areaMinZ = area.z;
        final int areaMaxZ = area.z + area.height;

        // Non-recursive breadth-first traversal
        for (int i = 0; i < queueSize; i++) {
            PlayerQuadtree node = WORK_QUEUE[i];

            // Quick boundary test
            if (areaMaxX < node.minX || areaMinX > node.maxX ||
                areaMaxZ < node.minZ || areaMinZ > node.maxZ) {
                continue;
            }

            // Add players from this node
            ServerPlayer[] nodePlayers = node.players;
            int count = node.playerCount;
            for (int j = 0; j < count; j++) {
                ServerPlayer player = nodePlayers[j];
                int px = (int)player.getX();
                int pz = (int)player.getZ();
                if (px >= areaMinX && px < areaMaxX &&
                    pz >= areaMinZ && pz < areaMaxZ) {
                    results.add(player);
                }
            }

            // Queue child nodes if they exist
            if (node.nodes != null && queueSize < WORK_QUEUE.length) {
                PlayerQuadtree[] childNodes = node.nodes;

                // Directly check overlap and queue relevant nodes
                int nodeMidX = node.midX;
                int nodeMidZ = node.midZ;

                if (areaMinZ <= nodeMidZ) {
                    if (areaMinX <= nodeMidX) {
                        WORK_QUEUE[queueSize++] = childNodes[0];
                    }
                    if (areaMaxX > nodeMidX) {
                        WORK_QUEUE[queueSize++] = childNodes[1];
                    }
                }
                if (areaMaxZ > nodeMidZ) {
                    if (areaMinX <= nodeMidX) {
                        WORK_QUEUE[queueSize++] = childNodes[2];
                    }
                    if (areaMaxX > nodeMidX) {
                        WORK_QUEUE[queueSize++] = childNodes[3];
                    }
                }
            }
        }

        return results;
    }

    /** Determines which node (if any) fully contains the given area */
    private int getIndex(Rectangle2D area) {
        double midX = bounds.x + bounds.width / 2.0;
        double midZ = bounds.z + bounds.height / 2.0;

        boolean topQuadrant = (area.z < midZ && area.z + area.height < midZ);
        boolean bottomQuadrant = (area.z >= midZ);

        if (area.x < midX && area.x + area.width < midX) {
            if (topQuadrant) return 0; // Top-left
            if (bottomQuadrant) return 2; // Bottom-left
        } else if (area.x >= midX) {
            if (topQuadrant) return 1; // Top-right
            if (bottomQuadrant) return 3; // Bottom-right
        }

        return -1; // Area overlaps multiple quadrants
    }

    /** Rectangle class for 2D (XZ) spatial partitioning */
    public static class Rectangle2D {
        public final int x, z, width, height;

        public Rectangle2D(int x, int z, int width, int height) {
            this.x = x;
            this.z = z;
            this.width = width;
            this.height = height;
        }
    }

    public void clear() {
        if (playerCount > 0) {
            // Clear player array efficiently
            Arrays.fill(players, 0, playerCount, null);
            playerCount = 0;
        }

        if (nodes != null) {
            for (PlayerQuadtree node : nodes) {
                node.clear();
            }
            nodes = null;
        }
    }
}
