package org.leavesmc.leaves.protocol.jade.accessor;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<BlockHitResult> implements BlockAccessor {

    private final BlockState blockState;
    @Nullable
    private final Supplier<BlockEntity> blockEntity;

    private BlockAccessorImpl(Builder builder) {
        super(builder.level, builder.player, Suppliers.ofInstance(builder.hit), builder.connected, builder.showDetails);
        blockState = builder.blockState;
        blockEntity = builder.blockEntity;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity == null ? null : blockEntity.get();
    }

    @Override
    public BlockPos getPosition() {
        return getHitResult().getBlockPos();
    }

    @Override
    public Direction getSide() {
        return getHitResult().getDirection();
    }

    @Nullable
    @Override
    public Object getTarget() {
        return getBlockEntity();
    }

    public static class Builder implements BlockAccessor.Builder {

        private Level level;
        private Player player;
        private boolean connected;
        private boolean showDetails;
        private BlockHitResult hit;
        private BlockState blockState = Blocks.AIR.defaultBlockState();
        private Supplier<BlockEntity> blockEntity;

        @Override
        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        @Override
        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        @Override
        public Builder showDetails(boolean showDetails) {
            this.showDetails = showDetails;
            return this;
        }

        @Override
        public Builder hit(BlockHitResult hit) {
            this.hit = hit;
            return this;
        }

        @Override
        public Builder blockState(BlockState blockState) {
            this.blockState = blockState;
            return this;
        }

        @Override
        public Builder blockEntity(Supplier<BlockEntity> blockEntity) {
            this.blockEntity = blockEntity;
            return this;
        }

        @Override
        public Builder from(BlockAccessor accessor) {
            level = accessor.getLevel();
            player = accessor.getPlayer();
            connected = accessor.isServerConnected();
            showDetails = accessor.showDetails();
            hit = accessor.getHitResult();
            blockEntity = accessor::getBlockEntity;
            blockState = accessor.getBlockState();
            return this;
        }

        @Override
        public BlockAccessor build() {
            return new BlockAccessorImpl(this);
        }
    }

    public record SyncData(boolean showDetails, BlockHitResult hit, BlockState blockState, ItemStack fakeBlock) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                SyncData::showDetails,
                StreamCodec.of(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult),
                SyncData::hit,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
                SyncData::blockState,
                ItemStack.OPTIONAL_STREAM_CODEC,
                SyncData::fakeBlock,
                SyncData::new
        );

        public BlockAccessor unpack(ServerPlayer player) {
            Supplier<BlockEntity> blockEntity = null;
            if (blockState.hasBlockEntity()) {
                blockEntity = Suppliers.memoize(() -> player.level().getBlockEntity(hit.getBlockPos()));
            }
            return new Builder()
                    .level(player.level())
                    .player(player)
                    .showDetails(showDetails)
                    .hit(hit)
                    .blockState(blockState)
                    .blockEntity(blockEntity)
                    .build();
        }
    }
}
