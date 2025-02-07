package org.leavesmc.leaves.protocol.jade.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;

public record ClientHandshakePayload(String protocolVersion) implements LeavesCustomPayload<ClientHandshakePayload> {

    private static final ResourceLocation PACKET_CLIENT_HANDSHAKE = JadeProtocol.id("client_handshake");

    private static final StreamCodec<RegistryFriendlyByteBuf, ClientHandshakePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        ClientHandshakePayload::protocolVersion,
        ClientHandshakePayload::new);

    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(ProtocolUtils.decorate(buf), this);
    }

    @Override
    public ResourceLocation id() {
        return PACKET_CLIENT_HANDSHAKE;
    }

    @New
    public static ClientHandshakePayload create(ResourceLocation location, FriendlyByteBuf buf) {
        return CODEC.decode(ProtocolUtils.decorate(buf));
    }
}
