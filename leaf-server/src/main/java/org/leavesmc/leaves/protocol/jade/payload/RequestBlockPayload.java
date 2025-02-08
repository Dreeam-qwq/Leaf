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
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessorImpl;
import org.leavesmc.leaves.protocol.jade.provider.IServerDataProvider;

import java.util.List;
import java.util.Objects;

import static org.leavesmc.leaves.protocol.jade.JadeProtocol.blockDataProviders;

public record RequestBlockPayload(BlockAccessorImpl.SyncData data, List<@Nullable IServerDataProvider<BlockAccessor>> dataProviders) implements LeavesCustomPayload<RequestBlockPayload> {

    private static final ResourceLocation PACKET_REQUEST_BLOCK = JadeProtocol.id("request_block");
    private static final StreamCodec<RegistryFriendlyByteBuf, RequestBlockPayload> CODEC = StreamCodec.composite(
            BlockAccessorImpl.SyncData.STREAM_CODEC,
            RequestBlockPayload::data,
            ByteBufCodecs.<ByteBuf, IServerDataProvider<BlockAccessor>>list()
                    .apply(ByteBufCodecs.idMapper(
                            $ -> Objects.requireNonNull(blockDataProviders.idMapper()).byId($),
                            $ -> Objects.requireNonNull(blockDataProviders.idMapper()).getIdOrThrow($))),
            RequestBlockPayload::dataProviders,
            RequestBlockPayload::new);

    @Override
    public void write(FriendlyByteBuf buf) {
        CODEC.encode(ProtocolUtils.decorate(buf), this);
    }

    @Override
    @NotNull
    public ResourceLocation id() {
        return PACKET_REQUEST_BLOCK;
    }

    @New
    public static RequestBlockPayload create(ResourceLocation location, FriendlyByteBuf buf) {
        return CODEC.decode(ProtocolUtils.decorate(buf));
    }
}
