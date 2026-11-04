package com.github.elenterius.biomancy.entity.mob.fleshblob;

import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.component.DataComponents;
import com.github.elenterius.biomancy.entity.mob.FoodEater;
import com.github.elenterius.biomancy.entity.mob.ai.goal.FindItemGoal;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.CustomInstructionKeyframeData;
import com.geckolib.animation.object.PlayState;

import java.util.function.Predicate;

public abstract class EaterFleshBlob extends FleshBlob implements FoodEater {

	public static final Predicate<ItemEntity> ITEM_ENTITY_FILTER = itemEntity -> FindItemGoal.ITEM_ENTITY_FILTER.test(itemEntity) && itemEntity.getItem().get(DataComponents.FOOD) != null;
	protected static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(EaterFleshBlob.class, EntityDataSerializers.BOOLEAN);

	protected EaterFleshBlob(EntityType<? extends EaterFleshBlob> entityType, Level level) {
		super(entityType, level);
		setCanPickUpLoot(true);
	}

	protected static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
		ItemStack stack = itemEntity.getItem();
		ItemStack splitStack = stack.split(1);

		if (stack.isEmpty()) itemEntity.discard();
		else itemEntity.setItem(stack);

		return splitStack;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(IS_EATING, false);
	}

	protected float getFoodHealAmount(@Nullable FoodProperties food, boolean isMeat) {
		if (food == null) return 0.5f;
		return food.nutrition() * (isMeat ? 1.25f : 0.75f);
	}

	protected float getGrowChance(@Nullable FoodProperties food, boolean isMeat) {
		if (food == null) return 0.4f;
		return 0.4f + (food.nutrition() * (isMeat ? 0.5f : 0.25f)) / MAX_SIZE;
	}

	private static boolean isEdible(ItemStack stack) {
		return stack.get(DataComponents.FOOD) != null;
	}

	@Override
	public boolean canPickUpLoot() {
		if (tickCount < 20 * 5) return false;

		ItemStack heldStack = getFoodItem();
		return (heldStack.isEmpty() || !isEdible(heldStack)) && super.canPickUpLoot();
	}

	public boolean canTakeItem(ItemStack stack) {
		if (isEdible(stack)) {
			return getFoodItem().isEmpty() && canPickUpLoot();
		}
		return true;
	}

	@Override
	public boolean canHoldItem(ItemStack stack) {
		ItemStack heldStack = getFoodItem();
		return isEdible(stack) && (heldStack.isEmpty() || !isEdible(heldStack));
	}

	@Override
	protected void pickUpItem(ServerLevel level, ItemEntity itemEntity) {
		ItemStack stack = itemEntity.getItem();
		if (canHoldItem(stack)) {
			onItemPickup(itemEntity);
			take(itemEntity, 1);
			stack = removeOneItemFromItemEntity(itemEntity);

			ItemStack heldStack = getFoodItem();
			if (!heldStack.isEmpty()) {
				spawnAtLocation(level, heldStack);
			}

			setFoodItem(stack);
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (canHoldItem(stack)) {
			if (level().isClientSide()) return InteractionResult.CONSUME;

			ItemStack heldStack = getFoodItem();
			if (!heldStack.isEmpty() && level() instanceof ServerLevel serverLevel) {
				spawnAtLocation(serverLevel, heldStack);
			}

			setFoodItem(stack.copyWithCount(1));
			if (!player.getAbilities().instabuild) stack.shrink(1);

			if (!isSilent()) {
				level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EAT, getSoundSource(), 0.75f + 0.25f * random.nextInt(2), (random.nextFloat() - random.nextFloat()) * 0.2f + 1f);
			}
			gameEvent(GameEvent.EAT, this);

			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(player, hand);
	}

	@Override
	public boolean isEating() {
		return entityData.get(IS_EATING);
	}

	@Override
	public void setEating(boolean flag) {
		entityData.set(IS_EATING, flag);
	}

	@Override
	public void ate(@Nullable FoodProperties food, boolean isMeat) {
		float health = getHealth();
		if (health < getMaxHealth()) {
			heal(getFoodHealAmount(food, isMeat));
		}
		else {
			byte blobSize = getBlobSize();
			if (blobSize < MAX_SIZE && random.nextFloat() < getGrowChance(food, isMeat)) {
				setBlobSize((byte) (blobSize + 1), true);
			}
		}
	}

	@Override
	public ItemStack getFoodItem() {
		return getItemBySlot(EquipmentSlot.MAINHAND);
	}

	@Override
	public void setFoodItem(ItemStack stack) {
		setItemSlot(EquipmentSlot.MAINHAND, stack);
		setGuaranteedDrop(EquipmentSlot.MAINHAND);
	}

	protected void spawnEatingParticles(ItemStack stack) {
		float pitch = -getXRot() * Mth.DEG_TO_RAD;
		float yaw = -getYRot() * Mth.DEG_TO_RAD;

		double radius = getDimensions(getPose()).width() / 2d;
		double x = getX() + getLookAngle().x * radius;
		double y = getY();
		double z = getZ() + getLookAngle().z * radius;

		for (int i = 0; i < 8; i++) {
			Vec3 motion = new Vec3((random.nextFloat() - 0.5d) * 0.1d, random.nextFloat() * 0.1d + 0.15d, 0).xRot(pitch).yRot(yaw);
			level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)), x, y, z, motion.x, motion.y, motion.z);
		}
	}

	protected void playEatingFX() {
		ItemStack stack = getFoodItem();
		if (stack.isEmpty()) return;

		if (stack.getUseAnimation() == ItemUseAnimation.DRINK) {
			playClientLocalSound(getConsumeSound(stack, SoundEvents.GENERIC_DRINK.value()), 0.75f, level().getRandom().nextFloat() * 0.1f + 0.9f);
		}

		if (stack.getUseAnimation() == ItemUseAnimation.EAT) {
			spawnEatingParticles(stack);
			SoundEvent eatingSound = getConsumeSound(stack, SoundEvents.GENERIC_EAT.value());
			playClientLocalSound(eatingSound, 0.75f + 0.25f * random.nextInt(2), (random.nextFloat() - random.nextFloat()) * 0.2f + 1f);
		}
	}

	private static SoundEvent getConsumeSound(ItemStack stack, SoundEvent fallback) {
		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		return consumable != null ? consumable.sound().value() : fallback;
	}

	private void playClientLocalSound(SoundEvent soundEvent, float volume, float pitch) {
		if (!isSilent()) {
			level().playLocalSound(getX(), getY(), getZ(), soundEvent, getSoundSource(), volume, pitch, false);
		}
	}

	protected <T extends EaterFleshBlob> PlayState handleEatingAnimation(AnimationTest<T> event) {
		if (isEating()) {
			event.controller().setAnimation(EATING_ANIMATION);
			return PlayState.CONTINUE;
		}

		return PlayState.STOP;
	}

	protected <E extends EaterFleshBlob> void onEatingSfx(KeyFrameEvent<E, CustomInstructionKeyframeData> event) {
		if (event.keyframeData().getInstructions().equals("eating_fx;")) {
			playEatingFX();
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		super.registerControllers(controllers);
		AnimationController<EaterFleshBlob> controller = new AnimationController<>("eat", 4, this::handleEatingAnimation);
		controller.setCustomInstructionKeyframeHandler(this::onEatingSfx);
		controllers.add(controller);
	}

}
