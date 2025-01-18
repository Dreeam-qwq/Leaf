package org.leavesmc.leaves.protocol.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface LeavesCustomPayload<T extends LeavesCustomPayload<T>> extends CustomPacketPayload {

    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @Retention(RetentionPolicy.RUNTIME)
    @interface New {
    }

    void write(FriendlyByteBuf buf);

    ResourceLocation id();

    @Override
    @NotNull
    default Type<T> type() {
        return new CustomPacketPayload.Type<>(id());
    }
}
