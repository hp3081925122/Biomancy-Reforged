package com.github.elenterius.biomancy.item.weapon;

import com.github.elenterius.biomancy.item.ConditionalEnchantable;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.init.ModEnchantments;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

public class DespoilingSwordItem extends SimpleSwordItem implements ConditionalEnchantable {

	public DespoilingSwordItem(ToolMaterial material, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(material, attackDamageModifier, attackSpeedModifier, properties);
	}

	public boolean isBroken(ItemStack stack) {
		return !isNotBroken(stack);
	}

	public boolean isNotBroken(ItemStack stack) {
		return getDamage(stack) < getMaxDamage(stack) - 1;
	}

	@Override
	public boolean isDamageable(ItemStack stack) {
		return isNotBroken(stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return stack.getMaxStackSize() == 1 && isDamageable(stack);
	}

	@Override
	public int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
		if (stack instanceof ItemStack itemStack && isBroken(itemStack)) return 0;

		int level = super.getEnchantmentLevel(stack, enchantment);

		if (enchantment.is(ModEnchantments.DESPOIL)) {
			return level + 1;
		}

		return level;
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (isBroken(stack)) return;

		if (!target.isDeadOrDying()) {
			stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
		}
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity livingEntity) {
		if (isBroken(stack)) return false;
		return super.mineBlock(stack, level, state, pos, livingEntity);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (isBroken(stack)) return 0f;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		if (isBroken(stack)) return false;
		return super.isCorrectToolForDrops(stack, state);
	}

	@Override
	public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
		if (stack instanceof ItemStack itemStack && isBroken(itemStack)) return false;
		return super.canPerformAction(stack, itemAbility);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		if (isBroken(stack)) return ItemAttributeModifiers.EMPTY;
		return super.getDefaultAttributeModifiers(stack);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return false;
	}

}
