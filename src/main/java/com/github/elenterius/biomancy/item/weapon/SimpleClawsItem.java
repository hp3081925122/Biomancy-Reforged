package com.github.elenterius.biomancy.item.weapon;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SimpleClawsItem extends ClawsItem implements ItemTooltipStyleProvider {

	public SimpleClawsItem(ToolMaterial material, float baseAttackDamage, float attackSpeedModifier, float attackRangeModifier, Properties properties) {
		super(material, baseAttackDamage, attackSpeedModifier, attackRangeModifier, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
	}

}
