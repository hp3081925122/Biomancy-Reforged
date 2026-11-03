package com.github.elenterius.biomancy.item.weapon;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SimpleSwordItem extends SwordItem implements ItemTooltipStyleProvider {

	private final ItemAttributeModifiers defaultAttributeModifiers;

	public SimpleSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, properties);
		defaultAttributeModifiers = SwordItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		return defaultAttributeModifiers;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		tooltip.addAll(ClientTextUtil.getItemInfoTooltip(stack));
	}

}
