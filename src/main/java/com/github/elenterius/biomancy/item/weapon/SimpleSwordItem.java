package com.github.elenterius.biomancy.item.weapon;

import com.github.elenterius.biomancy.init.ModItemAbilities;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import com.github.elenterius.biomancy.init.ModTiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SimpleSwordItem extends Item implements ItemTooltipStyleProvider {

	private final ItemAttributeModifiers defaultAttributeModifiers;

	public SimpleSwordItem(ToolMaterial material, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(ModTiers.applySwordProperties(material, properties));
		defaultAttributeModifiers = ModTiers.createSwordAttributes(material, attackDamageModifier, attackSpeedModifier);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		return defaultAttributeModifiers;
	}

	@Override
	public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
		return ModItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
	}

	@Override
	public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
		return !(user instanceof Player player && player.isCreative());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
	}

}
