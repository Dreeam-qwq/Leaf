package org.leavesmc.leaves.bot;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;

import java.io.File;

public class BotStatsCounter extends ServerStatsCounter {

    private static final File UNKOWN_FILE = new File("BOT_STATS_REMOVE_THIS");

    public BotStatsCounter(MinecraftServer server) {
        super(server, UNKOWN_FILE);
    }

    @Override
    public void save() {

    }

    @Override
    public void setValue(Player player, Stat<?> stat, int value) {

    }

    @Override
    public void parseLocal(DataFixer dataFixer, String json) {

    }

    @Override
    public int getValue(Stat<?> stat) {
        return 0;
    }
}
