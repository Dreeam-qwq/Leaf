package org.leavesmc.leaves.protocol.jade.tool;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleToolHandler implements ToolHandler {

    protected final List<ItemStack> tools = Lists.newArrayList();
    private final ResourceLocation uid;
    private final boolean skipInstaBreakingBlock;

    protected SimpleToolHandler(ResourceLocation uid, @NotNull List<ItemStack> tools, boolean skipInstaBreakingBlock) {
        this.uid = uid;
        Preconditions.checkArgument(!tools.isEmpty(), "tools cannot be empty");
        this.tools.addAll(tools);
        this.skipInstaBreakingBlock = skipInstaBreakingBlock;
    }

    @Contract("_, _ -> new")
    public static @NotNull SimpleToolHandler create(ResourceLocation uid, List<Item> tools) {
        return create(uid, tools, true);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull SimpleToolHandler create(ResourceLocation uid, List<Item> tools, boolean skipInstaBreakingBlock) {
        return new SimpleToolHandler(uid, Lists.transform(tools, Item::getDefaultInstance), skipInstaBreakingBlock);
    }

    @Override
    public ItemStack test(BlockState state, Level world, BlockPos pos) {
        if (skipInstaBreakingBlock && !state.requiresCorrectToolForDrops() && state.getDestroySpeed(world, pos) == 0) {
            return ItemStack.EMPTY;
        }
        return test(state);
    }

    public ItemStack test(BlockState state) {
        for (ItemStack toolItem : tools) {
            if (toolItem.isCorrectToolForDrops(state)) {
                return toolItem;
            }
            Tool tool = toolItem.get(DataComponents.TOOL);
            if (tool != null && tool.getMiningSpeed(state) > tool.defaultMiningSpeed()) {
                return toolItem;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> getTools() {
        return tools;
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }
}
