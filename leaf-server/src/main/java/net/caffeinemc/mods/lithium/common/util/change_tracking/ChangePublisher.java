package net.caffeinemc.mods.lithium.common.util.change_tracking;

import net.minecraft.world.item.ItemStack;

public interface ChangePublisher<T> {
    void subscribe(ChangeSubscriber<T> subscriber, int subscriberData);

    int unsubscribe(ChangeSubscriber<T> subscriber);

    default void unsubscribeWithData(ChangeSubscriber<T> subscriber, int index) {
        throw new UnsupportedOperationException("Only implemented for ItemStacks");
    }

    default boolean isSubscribedWithData(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        throw new UnsupportedOperationException("Only implemented for ItemStacks");
    }
}
