package com.github.elenterius.biomancy.item.weapon.gun;

import net.minecraft.core.component.DataComponents;
import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import com.github.elenterius.biomancy.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.entity.projectile.ImpalerProjectile;
import com.github.elenterius.biomancy.init.ModEntityTypes;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.animation.TriggerableAnimation;
import com.github.elenterius.biomancy.util.shooting.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.client.GeoRenderProvider;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ImpalerItem extends LivingGunItem<ImpalerProjectile> implements ItemTooltipStyleProvider, GeoItem {

	public static final float CHARGE_DURATION = 1.79f + 0.17f;

	protected static final Identifier BASE_MOVEMENT_SPEED_ID = BiomancyMod.rl("base_movement_speed");

	protected static final Identifier CROSSHAIR = BiomancyMod.rl("textures/gui/impaler_crosshair.png");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	private final ItemAttributeModifiers defaultModifiers;

	public ImpalerItem(int maxNutrients, Properties properties) {
		super(maxNutrients, properties.attributes(ItemAttributeModifiers.EMPTY).component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)),
				GunProperties.<ImpalerProjectile>builder()
						.shootBehavior(ShootBehavior.ON_RELEASE_INSTANT)
						.timeBetweenShots(Mth.ceil(CHARGE_DURATION * 20f))
						.damage(24f).accuracy(0.98f).spreadBias(SpreadBias.CENTER_HEAVY)
						.maxAmmo(1).reloadDuration(3 * 20).autoReload()
						.projectile(ModEntityTypes.IMPALER_PROJECTILE).velocity(2.85f)
						.localOffset(new Vector3f(0.25f, -0.2f, 0.5f))
						.sounds(GUN_SOUNDS.withShoot(ModSoundEvents.IMPALER_SHOOT.get()))
						.build()
		);

		defaultModifiers = ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.5f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
				.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(BASE_MOVEMENT_SPEED_ID, -0.125f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HAND)
				.build();

		GeoItem.registerSyncedAnimatable(this);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		return defaultModifiers;
	}

	@Override
	public int getReloadCost(ItemStack stack) {
		return 5;
	}

	@Override
	public int getDurabilityCost(ItemStack projectileWeapon) {
		return 35;
	}

	@Override
	public void onUseTick(Level level, LivingEntity shooter, ItemStack stack, int remainingUseDuration) {
		super.onUseTick(level, shooter, stack, remainingUseDuration);

		if (level.isClientSide()) return;
		if (getGunState(stack) != GunState.SHOOTING_OR_CHARGING) return;

		int elapsedTime = getUseDuration(stack, shooter) - remainingUseDuration;
		int delayBetweenShots = getDelayBetweenShots(stack);

		if (elapsedTime % delayBetweenShots == 0) {
			GunSounds.play(level, shooter, ModSoundEvents.IMPALER_CHARGE.get());
		}
	}

	@Override
	public float getProjectileVelocity(ItemStack stack) {
		return gunProperties.velocity() + 0.12f * EnchantmentUtil.getLevel(stack, Enchantments.POWER);
	}

	@Override
	public float getProjectileDamage(ItemStack stack) {
		return gunProperties.damage() + EnchantmentUtil.getLevel(stack, Enchantments.POWER);
	}

	@Override
	public void shoot(ServerLevel level, LivingEntity shooter, InteractionHand usedHand, ItemStack projectileWeapon) {
		float elapsedDuration = (float) projectileWeapon.getUseDuration(shooter) - ((float) shooter.getUseItemRemainingTicks());
		float maxChargeDuration = getDelayBetweenShots(projectileWeapon);
		float chargePercentage = Mth.clamp(elapsedDuration / maxChargeDuration, 0.1f, 1f);
		float velocity = getProjectileVelocity(projectileWeapon) * chargePercentage;

		boolean success = ProjectileUtil.shoot(level, shooter,
				velocity,
				getProjectileDamage(projectileWeapon),
				getProjectileKnockBack(projectileWeapon),
				getAccuracy(projectileWeapon),
				gunProperties.spreadBias(),
				usedHand,
				gunProperties.localOffset(),
				gunProperties.projectileType().get(),
				getProjectileCount(projectileWeapon),
				projectile -> projectile.setPierceLevel(EnchantmentUtil.getLevel(projectileWeapon, Enchantments.PIERCING)));

		if (!success) return;

		broadcastAnimation(level, shooter, projectileWeapon, Animations.SHOOT);
		gunProperties.sounds().playShoot(level, shooter, 1.5f, 0.8f + shooter.getRandom().nextFloat() * 0.5f);

		projectileWeapon.hurtAndBreak(1, shooter, usedHand.asEquipmentSlot());
		consumeAmmo(shooter, projectileWeapon, getAmmoCost(projectileWeapon));
		consumeNutrients(projectileWeapon, getDurabilityCost(projectileWeapon));

		boolean isAnchored = shooter.onGround() && shooter.isCrouching();
		double reduction = isAnchored ? 0.25d : 0.5d;

		Vec3 recoil = shooter.getLookAngle().normalize().scale(-1d).scale(velocity * reduction);
		shooter.push(recoil.x, recoil.y, recoil.z);
		shooter.fallDistance = 0f;

		if (!shooter.hurtMarked && shooter instanceof ServerPlayer serverPlayer) {
			serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
		}
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		if (enchantment.is(Enchantments.PIERCING)) return true;
		if (enchantment.is(Enchantments.PUNCH)) return false;
		return super.supportsEnchantment(stack, enchantment);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
		tooltip.accept(ComponentUtil.EMPTY_LINE);
		super.appendHoverText(stack, context, display, tooltip, isAdvanced);
	}

	@Override
	public void appendGunStats(ItemStack stack, Consumer<Component> tooltip) {
		if (ClientTextUtil.hasControlDown()) return;
		super.appendGunStats(stack, tooltip);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	protected void broadcastAnimation(ServerLevel level, Entity relatedEntity, ItemStack stack, TriggerableAnimation animation) {
		triggerAnim(relatedEntity, GeoItem.getOrAssignId(stack, level), animation.controller(), animation.name());
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		Animations.registerControllers(this, controllers);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public Identifier getCrosshairTexture(ItemStack stack, Player player) {
		return CROSSHAIR;
	}

	protected static final class Animations {
		static final String MAIN_CONTROLLER = "main";

		private static final List<TriggerableAnimation> TRIGGERABLE_ANIMATIONS = new ArrayList<>();

		static final TriggerableAnimation SHOOT = register(MAIN_CONTROLLER, "shoot", RawAnimation.begin().thenPlay("barrel_recoil"));
		static final RawAnimation CHARGE_UP_SHOT = RawAnimation.begin().thenPlay("charging_shot").thenPlay("holding_shot");
		static final RawAnimation DEFAULT_STATE = RawAnimation.begin().thenPlay("default_state");

		private Animations() {}

		static void registerControllers(ImpalerItem animatable, AnimatableManager.ControllerRegistrar controllers) {
			AnimationController<ImpalerItem> mainController = new AnimationController<>(MAIN_CONTROLLER, state -> PlayState.STOP);
			registerTriggerableAnimations(mainController);
			controllers.add(mainController);

			controllers.add(new AnimationController<>("charging", state -> {
				ImpalerItem impalerItem = (ImpalerItem) state.animatable();
				ItemStack stack = state.getData(ModDataTickets.ITEMSTACK);

				if (impalerItem.getGunState(stack) == GunState.SHOOTING_OR_CHARGING) {
					return state.setAndContinue(CHARGE_UP_SHOT);
				}

				return state.setAndContinue(DEFAULT_STATE);
			}));
		}

		private static TriggerableAnimation register(String controller, String name, RawAnimation rawAnimation) {
			TriggerableAnimation animation = new TriggerableAnimation(controller, name, rawAnimation);
			TRIGGERABLE_ANIMATIONS.add(animation);
			return animation;
		}

		private static void registerTriggerableAnimations(AnimationController<?> controller) {
			for (TriggerableAnimation animation : TRIGGERABLE_ANIMATIONS) {
				if (animation.controller().equals(controller.getName())) {
					controller.triggerableAnim(animation.name(), animation.rawAnimation());
				}
			}
		}

	}

}
