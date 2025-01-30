package net.caffeinemc.mods.lithium.common.entity;

import net.caffeinemc.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.world.item.ItemStack;

public interface EquipmentEntity {

    void onEquipmentReplaced(ItemStack oldStack, ItemStack newStack);

    interface EquipmentTrackingEntity {
        void onEquipmentChanged();
    }

    interface TickableEnchantmentTrackingEntity extends ChangeSubscriber.EnchantmentSubscriber<ItemStack> {
        void updateHasTickableEnchantments(ItemStack oldStack, ItemStack newStack);
    }
}
