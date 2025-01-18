package org.leavesmc.leaves.protocol.core;

import com.google.common.collect.ImmutableSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LeavesProtocolManager {

    private static final Class<?>[] PAYLOAD_PARAMETER_TYPES = {ResourceLocation.class, FriendlyByteBuf.class};

    private static final LeavesLogger LOGGER = LeavesLogger.LOGGER;

    private static final Map<LeavesProtocol, Map<ProtocolHandler.PayloadReceiver, Executable>> KNOWN_TYPES = new HashMap<>();
    private static final Map<LeavesProtocol, Map<ProtocolHandler.PayloadReceiver, Method>> KNOW_RECEIVERS = new HashMap<>();
    private static Set<ResourceLocation> ALL_KNOWN_ID = new HashSet<>();

    private static final List<Method> TICKERS = new ArrayList<>();
    private static final List<Method> PLAYER_JOIN = new ArrayList<>();
    private static final List<Method> PLAYER_LEAVE = new ArrayList<>();
    private static final List<Method> RELOAD_SERVER = new ArrayList<>();
    private static final Map<LeavesProtocol, Map<ProtocolHandler.MinecraftRegister, Method>> MINECRAFT_REGISTER = new HashMap<>();

    public static void reload() {
        handleServerReload();
        cleanProtocols(); // Do cleanup
        init();
    }

    public static void init() {
        boolean shouldEnable;

        for (Class<?> clazz : org.dreeam.leaf.config.LeafConfig.getClasses("org.leavesmc.leaves.protocol")) {
            final LeavesProtocol protocol = clazz.getAnnotation(LeavesProtocol.class);
            if (protocol != null) {
                Set<Method> methods;
                try {
                    Method[] publicMethods = clazz.getMethods();
                    Method[] privateMethods = clazz.getDeclaredMethods();
                    methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
                    Collections.addAll(methods, publicMethods);
                    Collections.addAll(methods, privateMethods);

                    Object instance = clazz.getConstructor().newInstance();
                    Method method = clazz.getMethod("shouldEnable");
                    shouldEnable = (boolean) method.invoke(instance);
                } catch (NoClassDefFoundError | InvocationTargetException | InstantiationException |
                         IllegalAccessException | NoSuchMethodException error) {
                    LOGGER.severe("Failed to load class " + clazz.getName() + " due to missing dependencies, " + error.getCause() + ": " + error.getMessage());
                    return;
                }

                Map<ProtocolHandler.PayloadReceiver, Executable> map = KNOWN_TYPES.getOrDefault(protocol, new HashMap<>());
                for (final Method method : methods) {
                    if (method.isBridge() || method.isSynthetic() || !Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }

                    method.setAccessible(true);

                    final ProtocolHandler.ReloadServer reloadServer = method.getAnnotation(ProtocolHandler.ReloadServer.class);
                    if (reloadServer != null) {
                        RELOAD_SERVER.add(method);
                        continue;
                    }

                    if (!shouldEnable) {
                        continue;
                    }

                    final ProtocolHandler.Init init = method.getAnnotation(ProtocolHandler.Init.class);
                    if (init != null) {
                        try {
                            method.invoke(null);
                        } catch (InvocationTargetException | IllegalAccessException exception) {
                            LOGGER.severe("Failed to invoke init method " + method.getName() + " in " + clazz.getName() + ", " + exception.getCause() + ": " + exception.getMessage());
                        }
                        continue;
                    }

                    final ProtocolHandler.PayloadReceiver receiver = method.getAnnotation(ProtocolHandler.PayloadReceiver.class);
                    if (receiver != null) {
                        try {
                            boolean found = false;
                            for (Method payloadMethod : receiver.payload().getDeclaredMethods()) {
                                if (payloadMethod.isAnnotationPresent(LeavesCustomPayload.New.class)) {
                                    if (Arrays.equals(payloadMethod.getParameterTypes(), PAYLOAD_PARAMETER_TYPES) && payloadMethod.getReturnType() == receiver.payload() && Modifier.isStatic(payloadMethod.getModifiers())) {
                                        payloadMethod.setAccessible(true);
                                        map.put(receiver, payloadMethod);
                                        found = true;
                                        break;
                                    }
                                }
                            }

                            if (!found) {
                                Constructor<? extends LeavesCustomPayload<?>> constructor = receiver.payload().getConstructor(PAYLOAD_PARAMETER_TYPES);
                                if (constructor.isAnnotationPresent(LeavesCustomPayload.New.class)) {
                                    constructor.setAccessible(true);
                                    map.put(receiver, constructor);
                                } else {
                                    throw new NoSuchMethodException();
                                }
                            }
                        } catch (NoSuchMethodException exception) {
                            LOGGER.severe("Failed to find constructor for " + receiver.payload().getName() + ", " + exception.getCause() + ": " + exception.getMessage());
                            continue;
                        }

                        if (!KNOW_RECEIVERS.containsKey(protocol)) {
                            KNOW_RECEIVERS.put(protocol, new HashMap<>());
                        }

                        KNOW_RECEIVERS.get(protocol).put(receiver, method);
                        continue;
                    }

                    final ProtocolHandler.Ticker ticker = method.getAnnotation(ProtocolHandler.Ticker.class);
                    if (ticker != null) {
                        TICKERS.add(method);
                        continue;
                    }

                    final ProtocolHandler.PlayerJoin playerJoin = method.getAnnotation(ProtocolHandler.PlayerJoin.class);
                    if (playerJoin != null) {
                        PLAYER_JOIN.add(method);
                        continue;
                    }

                    final ProtocolHandler.PlayerLeave playerLeave = method.getAnnotation(ProtocolHandler.PlayerLeave.class);
                    if (playerLeave != null) {
                        PLAYER_LEAVE.add(method);
                        continue;
                    }

                    final ProtocolHandler.MinecraftRegister minecraftRegister = method.getAnnotation(ProtocolHandler.MinecraftRegister.class);
                    if (minecraftRegister != null) {
                        if (!MINECRAFT_REGISTER.containsKey(protocol)) {
                            MINECRAFT_REGISTER.put(protocol, new HashMap<>());
                        }

                        MINECRAFT_REGISTER.get(protocol).put(minecraftRegister, method);
                    }
                }
                KNOWN_TYPES.put(protocol, map);
            }
        }

        for (LeavesProtocol protocol : KNOWN_TYPES.keySet()) {
            Map<ProtocolHandler.PayloadReceiver, Executable> map = KNOWN_TYPES.get(protocol);
            for (ProtocolHandler.PayloadReceiver receiver : map.keySet()) {
                if (receiver.sendFabricRegister() && !receiver.ignoreId()) {
                    for (String payloadId : receiver.payloadId()) {
                        for (String namespace : protocol.namespace()) {
                            ALL_KNOWN_ID.add(new ResourceLocation(namespace, payloadId));
                        }
                    }
                }
            }
        }
        ALL_KNOWN_ID = ImmutableSet.copyOf(ALL_KNOWN_ID);
    }

    private static void cleanProtocols() {
        KNOWN_TYPES.clear();
        KNOW_RECEIVERS.clear();
        //ALL_KNOWN_ID.clear(); // No need
        TICKERS.clear();
        PLAYER_JOIN.clear();
        PLAYER_LEAVE.clear();
        //RELOAD_SERVER.clear(); // No need
        MINECRAFT_REGISTER.clear();
    }

    public static LeavesCustomPayload<?> decode(ResourceLocation id, FriendlyByteBuf buf) {
        for (LeavesProtocol protocol : KNOWN_TYPES.keySet()) {
            if (!ArrayUtils.contains(protocol.namespace(), id.getNamespace())) {
                continue;
            }

            Map<ProtocolHandler.PayloadReceiver, Executable> map = KNOWN_TYPES.get(protocol);
            for (ProtocolHandler.PayloadReceiver receiver : map.keySet()) {
                if (receiver.ignoreId() || ArrayUtils.contains(receiver.payloadId(), id.getPath())) {
                    try {
                        if (map.get(receiver) instanceof Constructor<?> constructor) {
                            return (LeavesCustomPayload<?>) constructor.newInstance(id, buf);
                        } else if (map.get(receiver) instanceof Method method) {
                            return (LeavesCustomPayload<?>) method.invoke(null, id, buf);
                        }
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                        LOGGER.warning("Failed to create payload for " + id + " in " + ArrayUtils.toString(protocol.namespace()) + ", " + exception.getCause() + ": " + exception.getMessage());
                        buf.readBytes(buf.readableBytes());
                        return new ErrorPayload(id, protocol.namespace(), receiver.payloadId());
                    }
                }
            }
        }
        return null;
    }

    public static void handlePayload(ServerPlayer player, LeavesCustomPayload<?> payload) {
        if (payload instanceof ErrorPayload errorPayload) {
            player.connection.disconnect(Component.literal("Payload " + Arrays.toString(errorPayload.packetID) + " from " + Arrays.toString(errorPayload.protocolID) + " error"), PlayerKickEvent.Cause.INVALID_PAYLOAD);
            return;
        }

        for (LeavesProtocol protocol : KNOW_RECEIVERS.keySet()) {
            if (!ArrayUtils.contains(protocol.namespace(), payload.type().id().getNamespace())) {
                continue;
            }

            Map<ProtocolHandler.PayloadReceiver, Method> map = KNOW_RECEIVERS.get(protocol);
            for (ProtocolHandler.PayloadReceiver receiver : map.keySet()) {
                if (payload.getClass() == receiver.payload()) {
                    if (receiver.ignoreId() || ArrayUtils.contains(receiver.payloadId(), payload.type().id().getPath())) {
                        try {
                            map.get(receiver).invoke(null, player, payload);
                        } catch (InvocationTargetException | IllegalAccessException exception) {
                            LOGGER.warning("Failed to handle payload " + payload.type().id() + " in " + ArrayUtils.toString(protocol.namespace()) + ", " + exception.getCause() + ": " + exception.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static void handleTick() {
        if (!TICKERS.isEmpty()) {
            try {
                for (Method method : TICKERS) {
                    method.invoke(null);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to tick, " + exception.getCause() + ": " + exception.getMessage());
            }
        }
    }

    public static void handlePlayerJoin(ServerPlayer player) {
        if (!PLAYER_JOIN.isEmpty()) {
            try {
                for (Method method : PLAYER_JOIN) {
                    method.invoke(null, player);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to handle player join, " + exception.getCause() + ": " + exception.getMessage());
            }
        }

        ProtocolUtils.sendPayloadPacket(player, new FabricRegisterPayload(ALL_KNOWN_ID));
    }

    public static void handlePlayerLeave(ServerPlayer player) {
        if (!PLAYER_LEAVE.isEmpty()) {
            try {
                for (Method method : PLAYER_LEAVE) {
                    method.invoke(null, player);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to handle player leave, " + exception.getCause() + ": " + exception.getMessage());
            }
        }
    }

    public static void handleServerReload() {
        if (!RELOAD_SERVER.isEmpty()) {
            try {
                for (Method method : RELOAD_SERVER) {
                    method.invoke(null);
                }
            } catch (InvocationTargetException | IllegalAccessException exception) {
                LOGGER.warning("Failed to handle server reload, " + exception.getCause() + ": " + exception.getMessage());
            }
        }
    }

    public static void handleMinecraftRegister(String channelId, ServerPlayer player) {
        for (LeavesProtocol protocol : MINECRAFT_REGISTER.keySet()) {
            String[] channel = channelId.split(":");
            if (!ArrayUtils.contains(protocol.namespace(), channel[0])) {
                continue;
            }

            Map<ProtocolHandler.MinecraftRegister, Method> map = MINECRAFT_REGISTER.get(protocol);
            for (ProtocolHandler.MinecraftRegister register : map.keySet()) {
                if (register.ignoreId() || ArrayUtils.contains(register.channelId(), channel[1])) {
                    try {
                        map.get(register).invoke(null, player, channel[1]);
                    } catch (InvocationTargetException | IllegalAccessException exception) {
                        LOGGER.warning("Failed to handle minecraft register, " + exception.getCause() + ": " + exception.getMessage());
                    }
                }
            }
        }
    }

    public record ErrorPayload(ResourceLocation id, String[] protocolID, String[] packetID) implements LeavesCustomPayload<ErrorPayload> {
        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
        }
    }

    public record EmptyPayload(ResourceLocation id) implements LeavesCustomPayload<EmptyPayload> {

        @New
        public EmptyPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(location);
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buf) {
        }
    }

    public record LeavesPayload(FriendlyByteBuf data, ResourceLocation id) implements LeavesCustomPayload<LeavesPayload> {

        @New
        public LeavesPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(new FriendlyByteBuf(buf.readBytes(buf.readableBytes())), location);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBytes(data);
        }
    }

    public record FabricRegisterPayload(
            Set<ResourceLocation> channels) implements LeavesCustomPayload<FabricRegisterPayload> {

        public static final ResourceLocation CHANNEL = ResourceLocation.withDefaultNamespace("register");

        @New
        public FabricRegisterPayload(ResourceLocation location, FriendlyByteBuf buf) {
            this(buf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            boolean first = true;

            ResourceLocation channel;
            for (Iterator<ResourceLocation> var3 = this.channels.iterator(); var3.hasNext(); buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII))) {
                channel = var3.next();
                if (first) {
                    first = false;
                } else {
                    buf.writeByte(0);
                }
            }
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }
}
