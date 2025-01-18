package org.leavesmc.leaves.protocol.jade.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.leavesmc.leaves.protocol.jade.provider.IJadeProvider;

import java.util.List;

public interface ToolHandler extends IJadeProvider {

	ItemStack test(BlockState state, Level world, BlockPos pos);

	List<ItemStack> getTools();

}
