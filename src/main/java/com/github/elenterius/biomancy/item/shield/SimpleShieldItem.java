package com.github.elenterius.biomancy.item.shield;

import com.github.elenterius.biomancy.item.SimpleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;
import java.util.Optional;

public class SimpleShieldItem extends SimpleItem {

	public SimpleShieldItem(Properties properties) {
		super(applyShieldProperties(properties));
		DispenserBlock.registerBehavior(this, EquipmentDispenseItemBehavior.INSTANCE);
	}

	public static Properties applyShieldProperties(Properties properties) {
		return properties
				.equippableUnswappable(EquipmentSlot.OFFHAND)
				.delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
						0.25f,
						1f,
						List.of(new BlocksAttacks.DamageReduction(90f, Optional.empty(), 0f, 1f)),
						new BlocksAttacks.ItemDamageFunction(3f, 1f, 1f),
						Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
						Optional.of(SoundEvents.SHIELD_BLOCK),
						Optional.of(SoundEvents.SHIELD_BREAK)
				))
				.component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		return InteractionResult.CONSUME.heldItemTransformedTo(stack);
	}

}
