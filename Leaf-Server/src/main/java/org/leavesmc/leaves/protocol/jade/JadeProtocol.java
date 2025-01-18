package org.leavesmc.leaves.protocol.jade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.leavesmc.leaves.LeavesLogger;
import org.leavesmc.leaves.protocol.core.LeavesProtocol;
import org.leavesmc.leaves.protocol.core.ProtocolHandler;
import org.leavesmc.leaves.protocol.core.ProtocolUtils;
import org.leavesmc.leaves.protocol.jade.accessor.BlockAccessor;
import org.leavesmc.leaves.protocol.jade.accessor.EntityAccessor;
import org.leavesmc.leaves.protocol.jade.payload.ReceiveDataPayload;
import org.leavesmc.leaves.protocol.jade.payload.RequestBlockPayload;
import org.leavesmc.leaves.protocol.jade.payload.RequestEntityPayload;
import org.leavesmc.leaves.protocol.jade.payload.ServerPingPayload;
import org.leavesmc.leaves.protocol.jade.provider.IJadeProvider;
import org.leavesmc.leaves.protocol.jade.provider.IServerDataProvider;
import org.leavesmc.leaves.protocol.jade.provider.IServerExtensionProvider;
import org.leavesmc.leaves.protocol.jade.provider.ItemStorageExtensionProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.BeehiveProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.BrewingStandProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.CampfireProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.ChiseledBookshelfProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.CommandBlockProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.FurnaceProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.HopperLockProvider;
import org.leavesmc.leaves.protocol.jade.provider.ItemStorageProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.JukeboxProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.LecternProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.MobSpawnerCooldownProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.ObjectNameProvider;
import org.leavesmc.leaves.protocol.jade.provider.block.RedstoneProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.AnimalOwnerProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.MobBreedingProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.MobGrowthProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.NextEntityDropProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.StatusEffectsProvider;
import org.leavesmc.leaves.protocol.jade.provider.entity.ZombieVillagerProvider;
import org.leavesmc.leaves.protocol.jade.util.HierarchyLookup;
import org.leavesmc.leaves.protocol.jade.util.LootTableMineableCollector;
import org.leavesmc.leaves.protocol.jade.util.PairHierarchyLookup;
import org.leavesmc.leaves.protocol.jade.util.PriorityStore;
import org.leavesmc.leaves.protocol.jade.util.WrappedHierarchyLookup;

import java.util.Collections;
import java.util.List;

@LeavesProtocol(namespace = "jade")
public class JadeProtocol {

    public static PriorityStore<ResourceLocation, IJadeProvider> priorities;
    private static List<Block> shearableBlocks = null;

    public static final String PROTOCOL_ID = "jade";

    public static final HierarchyLookup<IServerDataProvider<EntityAccessor>> entityDataProviders = new HierarchyLookup<>(Entity.class);
    public static final PairHierarchyLookup<IServerDataProvider<BlockAccessor>> blockDataProviders = new PairHierarchyLookup<>(new HierarchyLookup<>(Block.class), new HierarchyLookup<>(BlockEntity.class));
    public static final WrappedHierarchyLookup<IServerExtensionProvider<ItemStack>> itemStorageProviders = WrappedHierarchyLookup.forAccessor();

    public static boolean shouldEnable() {
        return org.dreeam.leaf.config.modules.network.ProtocolSupport.jadeProtocol;
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return new ResourceLocation(PROTOCOL_ID, path);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation mc_id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    @ProtocolHandler.Init
    public static void init() {
        priorities = new PriorityStore<>(IJadeProvider::getDefaultPriority, IJadeProvider::getUid);

        // core plugin
        blockDataProviders.register(BlockEntity.class, ObjectNameProvider.ForBlock.INSTANCE);

        // universal plugin
        entityDataProviders.register(Entity.class, ItemStorageProvider.getEntity());
        blockDataProviders.register(Block.class, ItemStorageProvider.getBlock());

        itemStorageProviders.register(Object.class, ItemStorageExtensionProvider.INSTANCE);
        itemStorageProviders.register(Block.class, ItemStorageExtensionProvider.INSTANCE);

        // vanilla plugin
        entityDataProviders.register(Entity.class, AnimalOwnerProvider.INSTANCE);
        entityDataProviders.register(LivingEntity.class, StatusEffectsProvider.INSTANCE);
        entityDataProviders.register(AgeableMob.class, MobGrowthProvider.INSTANCE);
        entityDataProviders.register(Tadpole.class, MobGrowthProvider.INSTANCE);
        entityDataProviders.register(Animal.class, MobBreedingProvider.INSTANCE);
        entityDataProviders.register(Allay.class, MobBreedingProvider.INSTANCE);

        entityDataProviders.register(Chicken.class, NextEntityDropProvider.INSTANCE);
        entityDataProviders.register(Armadillo.class, NextEntityDropProvider.INSTANCE);

        entityDataProviders.register(ZombieVillager.class, ZombieVillagerProvider.INSTANCE);

        blockDataProviders.register(BrewingStandBlockEntity.class, BrewingStandProvider.INSTANCE);
        blockDataProviders.register(BeehiveBlockEntity.class, BeehiveProvider.INSTANCE);
        blockDataProviders.register(CommandBlockEntity.class, CommandBlockProvider.INSTANCE);
        blockDataProviders.register(JukeboxBlockEntity.class, JukeboxProvider.INSTANCE);
        blockDataProviders.register(LecternBlockEntity.class, LecternProvider.INSTANCE);

        blockDataProviders.register(ComparatorBlockEntity.class, RedstoneProvider.INSTANCE);
        blockDataProviders.register(HopperBlockEntity.class, HopperLockProvider.INSTANCE);
        blockDataProviders.register(CalibratedSculkSensorBlockEntity.class, RedstoneProvider.INSTANCE);

        blockDataProviders.register(AbstractFurnaceBlockEntity.class, FurnaceProvider.INSTANCE);
        blockDataProviders.register(ChiseledBookShelfBlockEntity.class, ChiseledBookshelfProvider.INSTANCE);
        blockDataProviders.register(TrialSpawnerBlockEntity.class, MobSpawnerCooldownProvider.INSTANCE);

        blockDataProviders.idMapped();
        entityDataProviders.idMapped();
        itemStorageProviders.register(CampfireBlock.class, CampfireProvider.INSTANCE);

        blockDataProviders.loadComplete(priorities);
        entityDataProviders.loadComplete(priorities);
        itemStorageProviders.loadComplete(priorities);

        try {
            shearableBlocks = Collections.unmodifiableList(LootTableMineableCollector.execute(
                    MinecraftServer.getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE),
                    Items.SHEARS.getDefaultInstance()
            ));
        } catch (Throwable ignore) {
            shearableBlocks = List.of();
            LeavesLogger.LOGGER.severe("Failed to collect shearable blocks");
        }
    }

    @ProtocolHandler.PlayerJoin
    public static void onPlayerJoin(ServerPlayer player) {
        sendPingPacket(player);
    }

    @ProtocolHandler.PayloadReceiver(payload = RequestEntityPayload.class, payloadId = "request_entity")
    public static void requestEntityData(ServerPlayer player, RequestEntityPayload payload) {
        MinecraftServer.getServer().execute(() -> {
            EntityAccessor accessor = payload.data().unpack(player);
            if (accessor == null) {
                return;
            }

            Entity entity = accessor.getEntity();
            double maxDistance = Mth.square(player.entityInteractionRange() + 21);
            if (entity == null || player.distanceToSqr(entity) > maxDistance) {
                return;
            }

            List<IServerDataProvider<EntityAccessor>> providers = entityDataProviders.get(entity);
            if (providers.isEmpty()) {
                return;
            }

            CompoundTag tag = new CompoundTag();
            for (IServerDataProvider<EntityAccessor> provider : providers) {
                if (!payload.dataProviders().contains(provider)) {
                    continue;
                }
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    LeavesLogger.LOGGER.warning("Error while saving data for entity " + entity);
                }
            }
            tag.putInt("EntityId", entity.getId());

            ProtocolUtils.sendPayloadPacket(player, new ReceiveDataPayload(tag));
        });
    }

    @ProtocolHandler.PayloadReceiver(payload = RequestBlockPayload.class, payloadId = "request_block")
    public static void requestBlockData(ServerPlayer player, RequestBlockPayload payload) {
        MinecraftServer server = MinecraftServer.getServer();
        server.execute(() -> {
            BlockAccessor accessor = payload.data().unpack(player);
            if (accessor == null) {
                return;
            }

            BlockPos pos = accessor.getPosition();
            Block block = accessor.getBlock();
            BlockEntity blockEntity = accessor.getBlockEntity();
            double maxDistance = Mth.square(player.blockInteractionRange() + 21);
            if (pos.distSqr(player.blockPosition()) > maxDistance || !accessor.getLevel().isLoaded(pos)) {
                return;
            }

            List<IServerDataProvider<BlockAccessor>> providers;
            if (blockEntity != null) {
                providers = blockDataProviders.getMerged(block, blockEntity);
            } else {
                providers = blockDataProviders.first.get(block);
            }

            if (providers.isEmpty()) {
                return;
            }

            CompoundTag tag = new CompoundTag();
            for (IServerDataProvider<BlockAccessor> provider : providers) {
                if (!payload.dataProviders().contains(provider)) {
                    continue;
                }
                try {
                    provider.appendServerData(tag, accessor);
                } catch (Exception e) {
                    LeavesLogger.LOGGER.warning("Error while saving data for block " + accessor.getBlockState());
                }
            }
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putString("BlockId", BuiltInRegistries.BLOCK.getKey(block).toString());

            ProtocolUtils.sendPayloadPacket(player, new ReceiveDataPayload(tag));
        });
    }

    @ProtocolHandler.ReloadServer
    public static void onServerReload() {
        if (org.dreeam.leaf.config.modules.network.ProtocolSupport.jadeProtocol) {
            enableAllPlayer();
        }
    }

    public static void enableAllPlayer() {
        for (ServerPlayer player : MinecraftServer.getServer().getPlayerList().players) {
            sendPingPacket(player);
        }
    }

    public static void sendPingPacket(ServerPlayer player) {
        ProtocolUtils.sendPayloadPacket(player, new ServerPingPayload(Collections.emptyMap(), shearableBlocks, blockDataProviders.mappedIds(), entityDataProviders.mappedIds()));
    }
}
