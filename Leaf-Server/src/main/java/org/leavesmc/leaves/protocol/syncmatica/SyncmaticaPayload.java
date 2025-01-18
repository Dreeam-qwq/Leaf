package org.leavesmc.leaves.protocol.syncmatica;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.leavesmc.leaves.protocol.core.LeavesCustomPayload;

public record SyncmaticaPayload(ResourceLocation packetType,
                                FriendlyByteBuf data) implements LeavesCustomPayload<SyncmaticaPayload> {

    private static final ResourceLocation NETWORK_ID = new ResourceLocation(SyncmaticaProtocol.PROTOCOL_ID, "main");

    @New
    public static SyncmaticaPayload decode(ResourceLocation location, FriendlyByteBuf buf) {
        return new SyncmaticaPayload(buf.readResourceLocation(), new FriendlyByteBuf(buf.readBytes(buf.readableBytes())));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.packetType);
        buf.writeBytes(this.data.readBytes(this.data.readableBytes()));
    }

    @Override
    public ResourceLocation id() {
        return NETWORK_ID;
    }
}
