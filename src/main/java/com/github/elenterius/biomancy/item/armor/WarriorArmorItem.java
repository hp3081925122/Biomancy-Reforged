package com.github.elenterius.biomancy.item.armor;

import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.component.BulletJumpState;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.entity.misc.LivingEntityData;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.init.client.ModKeyBindings;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.item.KeyPressListener;
import com.github.elenterius.biomancy.item.KnowledgeReader;
import com.github.elenterius.biomancy.sounds.ServerSoundHandler;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.CombatUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.client.GeoRenderProvider;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WarriorArmorItem extends LivingArmorGeoItem implements KnowledgeReader, ItemTooltipStyleProvider, KeyPressListener {

	public static final Predicate<ItemStack> ARMOR_SET_PREDICATE = itemStack -> itemStack.getItem() instanceof WarriorArmorItem;

	public static final int[] BULLET_JUMP_COST = {10, 15, 25, 40};
	public static final int BULLET_JUMP_COST_COOLDOWN = 5 * 20;

	public static final int IMPOSING_ROAR_COST = 100;
	public static final int IMPOSING_ROAR_DURATION = 30 * 20;
	public static final double IMPOSING_ROAR_RADIUS = 16d;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	protected final Lazy<ItemAttributeModifiers> defaultAttributeModifiers;
	protected final ItemAttributeModifiers brokenAttributeModifiers;

	public WarriorArmorItem(ArmorMaterial material, ArmorType type, int maxNutrients, Properties properties) {
		super(material, type, maxNutrients, properties);

		Identifier id = Identifier.withDefaultNamespace("armor." + type.getName());
		EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());

		ItemAttributeModifiers.Builder brokenBuilder = ItemAttributeModifiers.builder();
		if (type == ArmorType.CHESTPLATE) {
			brokenBuilder.add(Attributes.ATTACK_SPEED, new AttributeModifier(id, -0.1d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
			brokenBuilder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, -0.2d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
		}
		brokenAttributeModifiers = brokenBuilder.build();

		defaultAttributeModifiers = Lazy.of(() -> {
			ItemAttributeModifiers.Builder defaultBuilder = ItemAttributeModifiers.builder();
			for (ItemAttributeModifiers.Entry entry : material.createAttributes(type).modifiers()) {
				defaultBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
			}

			if (type == ArmorType.CHESTPLATE) {
				defaultBuilder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(id, 0.10d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
				defaultBuilder.add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(id, 0.5d, AttributeModifier.Operation.ADD_VALUE), slotGroup);
				defaultBuilder.add(Attributes.ATTACK_SPEED, new AttributeModifier(id, -0.1d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
				defaultBuilder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, -0.2d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
			}
			else if (type == ArmorType.LEGGINGS) {
				defaultBuilder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, 0.2d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slotGroup);
			}

			return defaultBuilder.build();
		});
	}

	@Override
	protected ItemAttributeModifiers getArmorAttributeModifiers() {
		return defaultAttributeModifiers.get();
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		return getNutrients(stack) > 0 ? getArmorAttributeModifiers() : brokenAttributeModifiers;
	}

	@Override
	public KeyPressResult onClientKeyPress(ItemStack stack, Level level, Player player, EquipmentSlot slot, byte flags) {
		if (slot != EquipmentSlot.HEAD || type != ArmorType.HELMET) return KeyPressResult.fail();
		if (!CombatUtil.hasFulLArmorSetEquipped(player, ARMOR_SET_PREDICATE)) return KeyPressResult.fail();

		int nutrients = getNutrientsFromEquippedArmor(player, ARMOR_SET_PREDICATE);
		if (nutrients < IMPOSING_ROAR_COST) {
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("not_enough_nutrients"));
			player.playSound(ModSoundEvents.FLESHKIN_NO.get(), 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
			return KeyPressResult.fail();
		}

		return KeyPressResult.success(flags);
	}

	@Override
	public void onServerReceiveKeyPress(ItemStack stack, ServerLevel level, Player player, byte flags) {
		if (type != ArmorType.HELMET || player.getItemBySlot(EquipmentSlot.HEAD) != stack) return;
		if (!CombatUtil.hasFulLArmorSetEquipped(player, ARMOR_SET_PREDICATE)) return;

		int nutrients = getNutrientsFromEquippedArmor(player, ARMOR_SET_PREDICATE);
		if (nutrients >= IMPOSING_ROAR_COST) {
			consumeNutrientsFromEquippedArmor(player, IMPOSING_ROAR_COST, ARMOR_SET_PREDICATE);
			imposingRoar(level, player);

			int range = 256;
			float volume = range / 16f;
			level.playSound(null, player, ModSoundEvents.ARMOR_IMPOSING_ROAR.get(), SoundSource.PLAYERS, volume, 0.9f + player.getRandom().nextFloat() * 0.2f);
			player.gameEvent(GameEvent.ENTITY_ACTION);
		}
	}

	public float getFallReduction(ItemStack stack) {
		return type == ArmorType.BOOTS && getNutrients(stack) > 0 ? 0.5f : 0f;
	}

	public boolean onJump(ItemStack stack, LivingEntity livingEntity, boolean isBulletJumpKeyDown) {
		if (livingEntity.level().isClientSide()) return false;

		if (type != ArmorType.LEGGINGS) return false;
		if (livingEntity.isInWater()) return false;

		if (isBulletJumpKeyDown) {
			if (livingEntity instanceof ServerPlayer player) {
				int cost = getBulletJumpCost(player, stack);
				if (getNutrients(stack) >= cost) {
					if (bulletJump(player)) {
						consumeNutrients(stack, cost);
						increaseBulletJumpCost(player, stack);
						return true;
					}
				}
				else {
					player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("not_enough_nutrients"));
					ServerSoundHandler.triggerSoundForClientPlayer(player, ModSoundEvents.FLESHKIN_NO.get(), player.getSoundSource(), 1f, 1f + player.level().getRandom().nextFloat() * 0.4f);
				}
			}
			return false;
		}

		return false;
	}

	private static int getBulletJumpCost(ServerPlayer player, ItemStack stack) {
		BulletJumpState state = stack.getOrDefault(ModDataComponents.BULLET_JUMP, BulletJumpState.EMPTY);
		long timestamp = state.timestamp();
		long elapsedTime = player.level().getGameTime() - timestamp;

		if (elapsedTime > BULLET_JUMP_COST_COOLDOWN) {
			return BULLET_JUMP_COST[0];
		}

		int index = Mth.clamp(state.costIndex(), 0, BULLET_JUMP_COST.length - 1);
		return BULLET_JUMP_COST[index];
	}

	private static void increaseBulletJumpCost(ServerPlayer player, ItemStack stack) {
		BulletJumpState state = stack.getOrDefault(ModDataComponents.BULLET_JUMP, BulletJumpState.EMPTY);
		long timestamp = state.timestamp();
		long gameTime = player.level().getGameTime();
		long elapsedTime = gameTime - timestamp;

		int index;
		if (elapsedTime > BULLET_JUMP_COST_COOLDOWN) {
			index = 0;
		}
		else {
			index = Mth.clamp(state.costIndex() + 1, 0, BULLET_JUMP_COST.length - 1);
		}

		stack.set(ModDataComponents.BULLET_JUMP, new BulletJumpState(index, gameTime));
	}

	public void onFall(ItemStack stack, LivingFallEvent event) {
		if (type == ArmorType.BOOTS && getNutrients(stack) >= 1) {
			if (event.getDistance() > 3f && event.getDamageMultiplier() > 0f) {
				consumeNutrients(stack, 1);
			}
			float multiplier = 1f - getFallReduction(stack);
			event.setDamageMultiplier(event.getDamageMultiplier() * multiplier);
			event.setDistance(event.getDistance() * multiplier);
		}
	}

	protected boolean bulletJump(ServerPlayer player) {
		Vec3 lookVec = player.getLookAngle();
		Vec3 velocity = player.getDeltaMovement();

		Vec3 UP = new Vec3(0, 1, 0);
		double verticalSimilarity = UP.dot(lookVec);

		double maxDistance = 32d;
		double minDistance = maxDistance * 0.7d;
		double horizontalSimilarity = 1d - Math.abs(verticalSimilarity);
		double lookDistance = Mth.lerp(horizontalSimilarity, minDistance, maxDistance);

		Vec3 eyePos = player.getEyePosition();
		Vec3 lookTarget = eyePos.add(lookVec.scale(lookDistance));

		if (verticalSimilarity < -(22.5d / 90d)) {
			ClipContext context = new ClipContext(player.position(), lookTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			if (player.level().clip(context).getType() == HitResult.Type.BLOCK) {
				ServerSoundHandler.triggerSoundForClientPlayer(player, ModSoundEvents.FLESHKIN_NO.get(), player.getSoundSource(), 0.5f, 0.5f + player.level().getRandom().nextFloat() * 0.5f);
				return false;
			}
		}

		Vec3 jumpDirection = lookTarget.subtract(player.position()).normalize();

		if (canElytraFly(player)) {
			double maxPower = 0.75d + velocity.y;
			double power = Mth.lerp(horizontalSimilarity, maxPower * 0.54d, maxPower);

			player.setDeltaMovement(
					velocity.x + jumpDirection.x * power,
					jumpDirection.y * power,
					velocity.z + jumpDirection.z * power
			);

			player.startFallFlying();
		}
		else {
			double maxPower = 2d + velocity.y;
			double power = Mth.lerp(horizontalSimilarity, maxPower * 0.7d, maxPower);

			player.setDeltaMovement(
					velocity.x + jumpDirection.x * power,
					jumpDirection.y * power,
					velocity.z + jumpDirection.z * power
			);

			player.startAutoSpinAttack(20, 8f, ItemStack.EMPTY);
			if (player.onGround()) {
				player.move(MoverType.SELF, new Vec3(0d, 1.1999999f, 0d));
			}
		}

		if (player.onGround()) {
			player.setShiftKeyDown(false);

			if (player instanceof LivingEntityData.TransientDataProvider provider) {
				provider.biomancy$getData().setDiscardFriction(true);
			}
		}

		player.needsSync = true;
		player.hurtMarked = true;

		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.ARMOR_BULLET_JUMP.get(), player.getSoundSource(), 1f, 1f);

		return true;
	}

	protected static boolean canElytraFly(Player player) {
		if (!player.isFallFlying() && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION)) {
			return LivingEntity.canGlideUsing(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
		}
		return false;
	}

	protected void imposingRoar(ServerLevel level, Player player) {
		double radiusSqr = IMPOSING_ROAR_RADIUS * IMPOSING_ROAR_RADIUS;
		Vec3 center = player.getBoundingBox().getCenter();

		Predicate<Entity> predicate = EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_CREATIVE_OR_SPECTATOR);
		List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(IMPOSING_ROAR_RADIUS), predicate);

		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity == player) continue;
			if (center.distanceToSqr(livingEntity.getX(), livingEntity.getY(0.5d), livingEntity.getZ()) >= radiusSqr) continue;

			if (livingEntity.isAlliedTo(player)) {
				livingEntity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, IMPOSING_ROAR_DURATION, 1));
				livingEntity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, IMPOSING_ROAR_DURATION));
			}
			else {
				livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, IMPOSING_ROAR_DURATION));

				if (livingEntity instanceof Mob mob) {
					for (WrappedGoal wrappedGoal : mob.goalSelector.getAvailableGoals()) {
						if (wrappedGoal.getGoal() instanceof PanicGoal) {
							livingEntity.setLastHurtByMob(player);
							livingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, IMPOSING_ROAR_DURATION));
						}
					}
				}
			}
		}

		player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, IMPOSING_ROAR_DURATION));
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	@Override
	public boolean canShowKnowledgeOverlay(ItemStack stack, Player player) {
		return AcolyteArmorUpgrades.hasUpgrade(stack, AcolyteArmorUpgrades.PRIMORDIAL_SIGHT) && hasNutrients(stack);
	}

	@Override
	public boolean canTranslatePrimordialRunes(ItemStack stack, Player player) {
		return false;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		AcolyteArmorUpgrades.appendHoverText(stack, tooltip);

		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(TextComponentUtil.getAbilityText("fleshkin_affinity").withStyle(ChatFormatting.GRAY));
		tooltip.accept(ComponentUtil.space().append(TextComponentUtil.getAbilityText("fleshkin_affinity.desc")).withStyle(ChatFormatting.DARK_GRAY));

		switch (type) {
			case HELMET -> {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				tooltip.accept(TextComponentUtil.getAbilityText("imposing_roar").withStyle(ChatFormatting.GRAY));
				tooltip.accept(ComponentUtil.space().append(TextComponentUtil.getAbilityText("imposing_roar.desc", IMPOSING_ROAR_COST, IMPOSING_ROAR_RADIUS, IMPOSING_ROAR_DURATION / 20)).withStyle(ChatFormatting.DARK_GRAY));
				tooltip.accept(ClientTextUtil.pressButtonTo(ComponentUtil.keybind(ModKeyBindings.EQUIPPED_ARMOR_ACTION), TextComponentUtil.getActionText("activate")).withStyle(TextStyles.DARK_GRAY));
			}
			case LEGGINGS -> {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				tooltip.accept(TextComponentUtil.getAbilityText("bullet_jump").withStyle(ChatFormatting.GRAY));
				tooltip.accept(ComponentUtil.space().append(TextComponentUtil.getAbilityText("bullet_jump.desc", BULLET_JUMP_COST[0], abilityCostToString(BULLET_JUMP_COST), BULLET_JUMP_COST_COOLDOWN / 20)).withStyle(ChatFormatting.DARK_GRAY));

			}
			case BOOTS -> {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				tooltip.accept(TextComponentUtil.getAbilityText("padded_soles").withStyle(ChatFormatting.GRAY));

				int pct = (int) (getFallReduction(stack) * 100f);
				tooltip.accept(ComponentUtil.space().append(pct + "% Fall Reduction").withStyle(ChatFormatting.DARK_GRAY));
			}
		}

		tooltip.accept(ComponentUtil.EMPTY_LINE);

		appendLivingToolTooltip(stack, tooltip);

		if (stack.isEnchanted()) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
		}
	}

	public static String abilityCostToString(int[] array) {
		if (array.length == 0) return "0";

		StringBuilder builder = new StringBuilder();
		for (int value : array) {
			builder.append(value);
			builder.append("/");
		}
		return builder.toString();
	}

}