package org.leavesmc.leaves.protocol.jade.accessor;

import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public interface Accessor<T extends HitResult> {

    Level getLevel();

    Player getPlayer();

    <D> Tag encodeAsNbt(StreamEncoder<RegistryFriendlyByteBuf, D> codec, D value);

    T getHitResult();

    /**
     * @return {@code true} if the dedicated server has Jade installed.
     */
    boolean isServerConnected();

    boolean showDetails();

    @Nullable
    Object getTarget();

    float tickRate();
}
