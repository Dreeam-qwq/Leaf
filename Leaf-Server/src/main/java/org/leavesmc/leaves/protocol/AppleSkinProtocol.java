package org.leavesmc.leaves.protocol;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@LeavesProtocol(namespace = "appleskin")
public class AppleSkinProtocol {

    public static final String PROTOCOL_ID = "appleskin";

    private static final ResourceLocation SATURATION_KEY = id("saturation");
    private static final ResourceLocation EXHAUSTION_KEY = id("exhaustion");
    private static final ResourceLocation NATURAL_REGENERATION_KEY = id("natural_regeneration");

    private static final float MINIMUM_EXHAUSTION_CHANGE_THRESHOLD = 0.01F;

    private static final Map<ServerPlayer, Float> previousSaturationLevels = new HashMap<>();
    private static final Map<ServerPlayer, Float> previousExhaustionLevels = new HashMap<>();
    private static final Map<ServerPlayer, Boolean> previousNaturalRegeneration = new HashMap<>();

    private static final Map<ServerPlayer, Set<String>> subscribedChannels = new HashMap<>();

    public static boolean shouldEnable() {
        return org.dreeam.leaf.config.modules.network.ProtocolSupport.appleskinProtocol;
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return new ResourceLocation(PROTOCOL_ID, path);
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        resetPlayerData(player);
    }

    @ProtocolHandler.PlayerLeave
    public static void onPlayerLoggedOut(@NotNull ServerPlayer player) {
            subscribedChannels.remove(player);
            resetPlayerData(player);
    }

    @ProtocolHandler.MinecraftRegister(ignoreId = true)
    public static void onPlayerSubscribed(@NotNull ServerPlayer player, String channel) {
        if (org.dreeam.leaf.config.modules.network.ProtocolSupport.appleskinProtocol) {
            subscribedChannels.computeIfAbsent(player, k -> new HashSet<>()).add(channel);
        }
    }

    @ProtocolHandler.Ticker
    public static void tick() {
            if (MinecraftServer.getServer().getTickCount() % org.dreeam.leaf.config.modules.network.ProtocolSupport.appleskinSyncTickInterval != 0) {
                return;
            }

            for (Map.Entry<ServerPlayer, Set<String>> entry : subscribedChannels.entrySet()) {
                ServerPlayer player = entry.getKey();
                FoodData data = player.getFoodData();

                for (String channel : entry.getValue()) {
                    switch (channel) {
                        case "saturation" -> {
                            float saturation = data.getSaturationLevel();
                            Float previousSaturation = previousSaturationLevels.get(player);
                            if (previousSaturation == null || saturation != previousSaturation) {
                                ProtocolUtils.sendPayloadPacket(player, SATURATION_KEY, buf -> buf.writeFloat(saturation));
                                previousSaturationLevels.put(player, saturation);
                            }
                        }

                        case "exhaustion" -> {
                            float exhaustion = data.exhaustionLevel;
                            Float previousExhaustion = previousExhaustionLevels.get(player);
                            if (previousExhaustion == null || Math.abs(exhaustion - previousExhaustion) >= MINIMUM_EXHAUSTION_CHANGE_THRESHOLD) {
                                ProtocolUtils.sendPayloadPacket(player, EXHAUSTION_KEY, buf -> buf.writeFloat(exhaustion));
                                previousExhaustionLevels.put(player, exhaustion);
                            }
                        }

                        case "natural_regeneration" -> {
                            boolean regeneration = player.serverLevel().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
                            Boolean previousRegeneration = previousNaturalRegeneration.get(player);
                            if (previousRegeneration == null || regeneration != previousRegeneration) {
                                ProtocolUtils.sendPayloadPacket(player, NATURAL_REGENERATION_KEY, buf -> buf.writeBoolean(regeneration));
                                previousNaturalRegeneration.put(player, regeneration);
                            }
                        }
                    }
                }
            }
    }

    @ProtocolHandler.ReloadServer
    public static void onServerReload() {
        if (!org.dreeam.leaf.config.modules.network.ProtocolSupport.appleskinProtocol) {
            disableAllPlayer();
        }
    }

    public static void disableAllPlayer() {
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().getPlayers()) {
            onPlayerLoggedOut(player);
        }
    }

    private static void resetPlayerData(@NotNull ServerPlayer player) {
        previousExhaustionLevels.remove(player);
        previousSaturationLevels.remove(player);
        previousNaturalRegeneration.remove(player);
    }
}
