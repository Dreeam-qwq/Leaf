package org.leavesmc.leaves.protocol;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@LeavesProtocol(namespace = "asteorbar")
public class AsteorBarProtocol {

    public static final String PROTOCOL_ID = "asteorbar";

    private static final ResourceLocation NETWORK_KEY = id("network");

    private static final Map<UUID, Float> previousSaturationLevels = new HashMap<>();
    private static final Map<UUID, Float> previousExhaustionLevels = new HashMap<>();

    private static final float THRESHOLD = 0.01F;

    private static final Set<ServerPlayer> players = new HashSet<>();

    public static boolean shouldEnable() {
        return org.dreeam.leaf.config.modules.network.ProtocolSupport.asteorBarProtocol;
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        resetPlayerData(player);
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        players.remove(player);
        resetPlayerData(player);
    }

    @ProtocolHandler.MinecraftRegister(ignoreId = true)
    public static void onPlayerSubscribed(@NotNull ServerPlayer player) {
        players.add(player);
    }

    @ProtocolHandler.Ticker
    public static void tick() {
            for (ServerPlayer player : players) {
                FoodData data = player.getFoodData();

                float saturation = data.getSaturationLevel();
                Float previousSaturation = previousSaturationLevels.get(player.getUUID());
                if (previousSaturation == null || saturation != previousSaturation) {
                    ProtocolUtils.sendPayloadPacket(player, NETWORK_KEY, buf -> {
                        buf.writeByte(1);
                        buf.writeFloat(saturation);
                    });
                    previousSaturationLevels.put(player.getUUID(), saturation);
                }

                float exhaustion = data.exhaustionLevel;
                Float previousExhaustion = previousExhaustionLevels.get(player.getUUID());
                if (previousExhaustion == null || Math.abs(exhaustion - previousExhaustion) >= THRESHOLD) {
                    ProtocolUtils.sendPayloadPacket(player, NETWORK_KEY, buf -> {
                        buf.writeByte(0);
                        buf.writeFloat(exhaustion);
                    });
                    previousExhaustionLevels.put(player.getUUID(), exhaustion);
                }
            }
    }

    @ProtocolHandler.ReloadServer
    public static void onServerReload() {
        if (!org.dreeam.leaf.config.modules.network.ProtocolSupport.asteorBarProtocol) {
            disableAllPlayer();
        }
    }

    public static void disableAllPlayer() {
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
            onPlayerLoggedOut(player);
        }
    }

    private static void resetPlayerData(@NotNull ServerPlayer player) {
        previousExhaustionLevels.remove(player.getUUID());
        previousSaturationLevels.remove(player.getUUID());
    }
}
