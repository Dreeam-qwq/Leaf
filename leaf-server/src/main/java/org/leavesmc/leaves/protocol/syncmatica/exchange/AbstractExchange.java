package org.leavesmc.leaves.protocol.syncmatica.exchange;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public abstract class AbstractExchange implements Exchange {

    private boolean success = false;
    private boolean finished = false;
    private final ExchangeTarget partner;

    protected AbstractExchange(final ExchangeTarget partner) {
        this.partner = partner;
    }

    @Override
    public ExchangeTarget getPartner() {
        return partner;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public void close(final boolean notifyPartner) {
        finished = true;
        success = false;
        onClose();
        if (notifyPartner) {
            sendCancelPacket();
        }
    }

    protected void sendCancelPacket() {
    }

    protected void onClose() {
    }

    protected void succeed() {
        finished = true;
        success = true;
        onClose();
    }

    protected static boolean checkUUID(final FriendlyByteBuf sourceBuf, final UUID targetId) {
        final int r = sourceBuf.readerIndex();
        final UUID sourceId = sourceBuf.readUUID();
        sourceBuf.readerIndex(r);
        return sourceId.equals(targetId);
    }
}
