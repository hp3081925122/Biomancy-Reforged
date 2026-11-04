package com.github.elenterius.biomancy.item.weapon;

import com.github.elenterius.biomancy.init.ModItemAbilities;
import net.minecraft.world.item.ItemInstance;
import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import com.github.elenterius.biomancy.init.ModTiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;

public class ClawsItem extends Item {

	protected static final Identifier BASE_ATTACK_RANGE_ID = BiomancyMod.rl("base_attack_range");
	protected final Lazy<ItemAttributeModifiers> defaultAttributeModifiers;

	public ClawsItem(ToolMaterial material, float baseAttackDamage, float attackSpeedModifier, float attackRangeModifier, Properties properties) {
		super(ModTiers.applyCommonProperties(material, properties).component(DataComponents.WEAPON, new Weapon(1)));
		float attackDamageModifier = baseAttackDamage + material.attackDamageBonus();
		defaultAttributeModifiers = Lazy.of(() -> createDefaultAttributeModifiers(attackDamageModifier, attackSpeedModifier, attackRangeModifier).build());
	}

	protected ItemAttributeModifiers.Builder createDefaultAttributeModifiers(float attackDamageModifier, float attackSpeedModifier, float attackRangeModifier) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamageModifier, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeedModifier, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_RANGE_ID, attackRangeModifier, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		return defaultAttributeModifiers.get();
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
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return getDestroySpeed(state);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
		if (state.getDestroySpeed(level, pos) != 0f) {
			stack.hurtAndBreak(2, miningEntity, EquipmentSlot.MAINHAND);
		}
		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState block) {
		return block.is(Blocks.COBWEB) || block.is(BlockTags.LEAVES);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		if (player.level().isClientSide()) return InteractionResult.PASS;
		if (shearInteractionTarget(stack, player, interactionTarget, usedHand)) return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}

	protected float getDestroySpeed(BlockState state) {
		if (state.is(Blocks.COBWEB)) return 15f;
		if (state.is(BlockTags.LEAVES)) return 15f;
		if (state.is(BlockTags.WOOL)) return 5f;
		return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1f;
	}

	protected boolean shearInteractionTarget(ItemStack stack, Player player, LivingEntity targetEntity, InteractionHand usedHand) {
		if (targetEntity instanceof IShearable shearingTarget) {
			BlockPos pos = targetEntity.blockPosition();

			if (shearingTarget.isShearable(player, stack, targetEntity.level(), pos)) {
				List<ItemStack> drops = shearingTarget.onSheared(player, stack, targetEntity.level(), pos);
				if (!(targetEntity.level() instanceof ServerLevel serverLevel)) return true;
				RandomSource rand = player.getRandom();
				drops.forEach(lootStack -> {
					ItemEntity itemEntity = targetEntity.spawnAtLocation(serverLevel, lootStack, 1f);
					if (itemEntity != null) {
						itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add((rand.nextFloat() - rand.nextFloat()) * 0.1f, rand.nextFloat() * 0.05f, (rand.nextFloat() - rand.nextFloat()) * 0.1f));
					}
				});
				stack.hurtAndBreak(1, targetEntity, usedHand);
			}
			return true;
		}

		return false;
	}

}
