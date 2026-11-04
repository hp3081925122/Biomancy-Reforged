package com.github.elenterius.biomancy.item;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.block.base.BlockTooltipProvider;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SimpleBlockItem extends BlockItem implements ItemTooltipStyleProvider {

	public SimpleBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
		super.appendHoverText(stack, context, display, tooltip, flag);
		if (getBlock() instanceof BlockTooltipProvider provider) {
			provider.appendHoverText(stack, context, display, tooltip, flag);
		}
	}

}
