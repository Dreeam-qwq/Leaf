package org.leavesmc.leaves.protocol.jade.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public interface BlockAccessor extends Accessor<BlockHitResult> {

    Block getBlock();

    BlockState getBlockState();

    BlockEntity getBlockEntity();

    BlockPos getPosition();

    Direction getSide();

    @ApiStatus.NonExtendable
    interface Builder {
        Builder level(Level level);

        Builder player(Player player);

        Builder showDetails(boolean showDetails);

        Builder hit(BlockHitResult hit);

        Builder blockState(BlockState state);

        default Builder blockEntity(BlockEntity blockEntity) {
            return blockEntity(() -> blockEntity);
        }

        Builder blockEntity(Supplier<BlockEntity> blockEntity);

        Builder from(BlockAccessor accessor);

        BlockAccessor build();
    }
}
