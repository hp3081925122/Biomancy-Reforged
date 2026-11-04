package com.github.elenterius.biomancy.item;

import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SimpleItem extends Item implements ItemTooltipStyleProvider {

	public SimpleItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
	}

}
