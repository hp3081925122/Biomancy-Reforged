package com.github.elenterius.biomancy.item;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import com.github.elenterius.biomancy.util.BlockItemUtil;
import com.github.elenterius.biomancy.block.chrysalis.Chrysalis;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.MobUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.github.elenterius.biomancy.block.chrysalis.Chrysalis.*;

public class ChrysalisBlockItem extends SimpleBlockItem {

	public ChrysalisBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		if (itemEntity.level().isClientSide()) return;

		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(itemEntity.getItem());
		if (compoundTag == null || !compoundTag.contains(ENTITY_KEY)) return;

		CompoundTag entityTag = compoundTag.getCompoundOrEmpty(ENTITY_KEY);
		Chrysalis.spawnEntity((ServerLevel) itemEntity.level(), itemEntity.getEyePosition(), entityTag);
	}

	public boolean isEmpty(ItemStack stack) {
		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		return compoundTag == null || !compoundTag.contains(ENTITY_KEY);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		if (player.level().isClientSide()) return InteractionResult.PASS;

		if (!isEmpty(stack)) {
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("already_full"));
			return InteractionResult.PASS;
		}

		boolean isCaptureAllowed = isCapturingSupported(interactionTarget) && (isCapturingAllowed(interactionTarget) || player.isCreative());
		if (!isCaptureAllowed) {
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("mob_too_powerful"));
			return InteractionResult.PASS;
		}

		if (stack.getCount() > 1) {
			ItemStack copy = stack.copy();
			copy.setCount(1);

			CompoundTag compoundTag = new CompoundTag();

			if (storeEntity(compoundTag, interactionTarget, true)) {
				BlockItemUtil.setBlockEntityData(copy, ModBlockEntities.CHRYSALIS.get(), compoundTag);

				playInsertSound(player);

				if (!player.isCreative()) stack.shrink(1);
				if (!player.getInventory().add(copy)) {
					player.drop(copy, true);
				}

				return InteractionResult.SUCCESS;
			}

			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("failed_to_store_mob"));
			return InteractionResult.PASS;
		}

		CompoundTag compoundTag = new CompoundTag();
		if (storeEntity(compoundTag, interactionTarget, true)) {
			BlockItemUtil.setBlockEntityData(stack, ModBlockEntities.CHRYSALIS.get(), compoundTag);

			playInsertSound(player);

			if (player.isCreative()) {
				player.setItemInHand(usedHand, stack);
			}
			return InteractionResult.SUCCESS;
		}

		player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("failed_to_store_mob"));
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.isSecondaryUseActive()) return InteractionResult.PASS;

		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		if (compoundTag == null || !compoundTag.contains(ENTITY_KEY)) return InteractionResult.PASS;

		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		if (level.mayInteract(player, context.getClickedPos()) && player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack)) {

			CompoundTag entityTag = compoundTag.getCompoundOrEmpty(ENTITY_KEY);

			if (spawnEntity((ServerLevel) level, context, entityTag)) {
				compoundTag.remove(ENTITY_KEY);
				BlockItemUtil.setBlockEntityData(stack, ModBlockEntities.CHRYSALIS.get(), compoundTag);

				playRemoveSound(player);
				return InteractionResult.CONSUME;
			}

			MutableComponent entityName = ComponentUtil.translatable(entityTag.getStringOr(ENTITY_NAME_KEY, ""));
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("failed_to_spawn_mob", entityName));
			return InteractionResult.FAIL;
		}

		player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("item_interaction_not_allowed"));
		return InteractionResult.FAIL;
	}

	private boolean spawnEntity(ServerLevel level, UseOnContext context, CompoundTag entityTag) {
		Entity entityToSpawn = EntityType.loadEntityRecursive(entityTag.getCompoundOrEmpty(ENTITY_DATA_KEY), level, EntitySpawnReason.LOAD, entity -> {
			Vec3 pos = MobUtil.getAdjustedSpawnPositionFor(context.getClickedPos(), context.getClickLocation(), context.getClickedFace(), entity);
			entity.snapTo(pos.x, pos.y, pos.z, Mth.wrapDegrees(level.getRandom().nextFloat() * 360), 0);

			if (entity instanceof LivingEntity living) {
				living.yHeadRot = living.getYRot();
				living.yBodyRot = living.getYRot();
			}

			entity.setDeltaMovement(0, 0, 0);
			entity.fallDistance = 0;
			return entity;
		});

		if (entityToSpawn != null) {
			if (!MobUtil.isEntityIdUnique(level, entityToSpawn)) {
				MobUtil.randomizeUUID(entityToSpawn);
			}
			return level.addFreshEntity(entityToSpawn);
		}

		return false;
	}

	private void playRemoveSound(Player player) {
		playSound(player, ModSoundEvents.CHRYSALIS_REMOVE.get());
	}

	private void playInsertSound(Player player) {
		playSound(player, ModSoundEvents.CHRYSALIS_INSERT.get());
	}

	private void playSound(Player player, SoundEvent soundEvent) {
		player.playSound(soundEvent, 0.9f, 0.3f + player.level().getRandom().nextFloat() * 0.25f);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);

		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		if (compoundTag == null || !compoundTag.contains(ENTITY_KEY)) return;

		CompoundTag tag = compoundTag.getCompoundOrEmpty(ENTITY_KEY);
		MutableComponent entityName = ComponentUtil.translatable(tag.getStringOr(ENTITY_NAME_KEY, ""));

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(TextComponentUtil.getTooltipText("contains", entityName).withStyle(TextStyles.ITALIC_GRAY));

		float volume = tag.getFloatOr(ENTITY_VOLUME_KEY, 0f);
		tooltip.accept(ComponentUtil.literal(String.format("%.1f m\u00B3", volume)).withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public Component getName(ItemStack stack) {
		Component itemName = super.getName(stack);
		MutableComponent entityName = getEntityTypeName(stack);

		if (entityName == null) {
			return itemName;
		}

		return entityName.append(" ").append(itemName);
	}

	@Nullable
	private MutableComponent getEntityTypeName(ItemStack stack) {
		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		if (compoundTag == null || !compoundTag.contains(ENTITY_KEY)) return null;

		CompoundTag tag = compoundTag.getCompoundOrEmpty(ENTITY_KEY);
		return ComponentUtil.translatable(tag.getStringOr(ENTITY_NAME_KEY, ""));
	}

}
