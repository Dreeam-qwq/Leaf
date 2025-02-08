package org.leavesmc.leaves.protocol.jade.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;
import org.leavesmc.leaves.protocol.jade.accessor.EntityAccessor;
import org.leavesmc.leaves.protocol.jade.accessor.EntityAccessorImpl;
import org.leavesmc.leaves.protocol.jade.provider.IServerDataProvider;

import java.util.List;
import java.util.Objects;

import static org.leavesmc.leaves.protocol.jade.JadeProtocol.entityDataProviders;

public record RequestEntityPayload(EntityAccessorImpl.SyncData data,
                                   List<@Nullable IServerDataProvider<EntityAccessor>> dataProviders) implements LeavesCustomPayload<RequestEntityPayload> {

    private static final ResourceLocation PACKET_REQUEST_ENTITY = JadeProtocol.id("request_entity");
    private static final StreamCodec<RegistryFriendlyByteBuf, RequestEntityPayload> CODEC = StreamCodec.composite(
        EntityAccessorImpl.SyncData.STREAM_CODEC,
        RequestEntityPayload::data,
        ByteBufCodecs.<ByteBuf, IServerDataProvider<EntityAccessor>>list()
            .apply(ByteBufCodecs.idMapper(
                $ -> Objects.requireNonNull(entityDataProviders.idMapper()).byId($),
                $ -> Objects.requireNonNull(entityDataProviders.idMapper()).getIdOrThrow($)
            )),
        RequestEntityPayload::dataProviders,
        RequestEntityPayload::new);


    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(ProtocolUtils.decorate(buf), this);
    }


    @Override
    @NotNull
    public ResourceLocation id() {
        return PACKET_REQUEST_ENTITY;
    }

    @New
    public static RequestEntityPayload create(ResourceLocation location, FriendlyByteBuf buf) {
        return CODEC.decode(ProtocolUtils.decorate(buf));
    }
}
