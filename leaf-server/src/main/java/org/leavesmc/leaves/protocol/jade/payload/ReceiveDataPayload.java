package org.leavesmc.leaves.protocol.jade.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;
import org.leavesmc.leaves.protocol.jade.JadeProtocol;

public record ReceiveDataPayload(CompoundTag tag) implements LeavesCustomPayload<ReceiveDataPayload> {

    private static final ResourceLocation PACKET_RECEIVE_DATA = JadeProtocol.id("receive_data");

    @New
    public ReceiveDataPayload(ResourceLocation id, FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    @Override
    public ResourceLocation id() {
        return PACKET_RECEIVE_DATA;
    }
}
