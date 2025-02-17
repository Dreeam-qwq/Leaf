package org.dreeam.leaf.util.tree;

import net.minecraft.server.level.ServerPlayer;

/**
 * Specialized list implementation for storing players in regions with direct array access
 * and optimized iteration.
 */
public class RegionPlayerList {
    private static final int INITIAL_CAPACITY = 4;
    private ServerPlayer[] players;
    private int size;

    public RegionPlayerList() {
        this.players = new ServerPlayer[INITIAL_CAPACITY];
        this.size = 0;
    }

    public void add(ServerPlayer player) {
        if (size == players.length) {
            // Grow array if needed
            ServerPlayer[] newPlayers = new ServerPlayer[players.length * 2];
            System.arraycopy(players, 0, newPlayers, 0, players.length);
            players = newPlayers;
        }
        players[size++] = player;
    }

    public void remove(ServerPlayer player) {
        for (int i = 0; i < size; i++) {
            if (players[i] == player) {
                // Found the player, shift remaining elements left
                int numMoved = size - i - 1;
                if (numMoved > 0) {
                    System.arraycopy(players, i + 1, players, i, numMoved);
                }
                players[--size] = null;
                return;
            }
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            players[i] = null;
        }
        size = 0;
    }

    /**
     * Get the raw player array and size for direct iteration.
     * The array may be larger than size - only iterate up to size.
     */
    public ServerPlayer[] getPlayers() {
        return players;
    }
}
