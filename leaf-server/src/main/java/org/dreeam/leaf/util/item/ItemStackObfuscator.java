package org.dreeam.leaf.util.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.dreeam.leaf.config.modules.misc.HiddenItemComponents;

import java.util.List;

public class ItemStackObfuscator {

    public static ItemStack stripMeta(final ItemStack itemStack, final boolean copyItemStack) {
        if (itemStack.isEmpty() || itemStack.getComponentsPatch().isEmpty()) return itemStack;

        final ItemStack copy = copyItemStack ? itemStack.copy() : itemStack;

        // Get the types which need to hide
        List<DataComponentType<?>> hiddenTypes = HiddenItemComponents.hiddenItemComponentTypes;

        if (hiddenTypes.isEmpty()) return copy;

        // Remove specified types
        for (DataComponentType<?> type : hiddenTypes) {
            // Only remove, no others
            copy.remove(type);
        }

        return copy;
    }
}
