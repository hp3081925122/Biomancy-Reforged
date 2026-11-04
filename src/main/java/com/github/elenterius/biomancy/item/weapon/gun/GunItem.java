package com.github.elenterius.biomancy.item.weapon.gun;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.entity.projectile.BaseProjectile;
import com.github.elenterius.biomancy.item.KeyPressListener;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.shooting.AmmoSupplier;
import com.github.elenterius.biomancy.util.shooting.Gun;
import com.github.elenterius.biomancy.util.shooting.GunProperties;
import com.github.elenterius.biomancy.util.shooting.GunState;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class GunItem<T extends BaseProjectile> extends ProjectileWeaponItem implements Gun<T>, KeyPressListener {

	protected final GunProperties<T> gunProperties;

	protected GunItem(Properties properties, GunProperties<T> gunProperties) {
		super(properties);
		this.gunProperties = gunProperties;
	}

	@Override
	public GunProperties<T> getGunProperties() {
		return gunProperties;
	}

	@Override
	public KeyPressResult onClientKeyPress(ItemStack stack, Level level, Player player, EquipmentSlot slot, byte flags) {
		GunState state = getGunState(stack);
		if (state == GunState.NONE && !canReload(stack, player)) {
			gunProperties.sounds().playLocalFail(level, player);
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("reload_not_possible"));
			return KeyPressResult.fail();
		}

		if (state == GunState.SHOOTING_OR_CHARGING) {
			return KeyPressResult.fail();
		}

		return KeyPressResult.success(flags);
	}

	@Override
	public void onServerReceiveKeyPress(ItemStack stack, ServerLevel level, Player player, byte flags) {
		GunState state = getGunState(stack);

		if (state == GunState.NONE) {
			startReload(stack, level, player);
			return;
		}

		if (state == GunState.RELOADING) {
			cancelReload(stack, level, player);
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);
		GunState state = getGunState(stack);

		if (getAmmo(stack) >= getAmmoCost(stack)) {
			if (state == GunState.RELOADING && level instanceof ServerLevel serverLevel) {
				cancelReload(stack, serverLevel, player);
			}
			setGunState(stack, GunState.SHOOTING_OR_CHARGING);
			player.startUsingItem(usedHand);
			return InteractionResult.CONSUME.heldItemTransformedTo(stack);
		}

		player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("not_enough_ammo"));
		return InteractionResult.FAIL;
	}

	@Override
	public void onUseTick(Level level, LivingEntity shooter, ItemStack stack, int remainingUseDuration) {
		if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
			if (getGunState(stack) != GunState.SHOOTING_OR_CHARGING) return;

			if (getAmmo(stack) < getAmmoCost(stack)) {
				if (shooter instanceof Player player) {
					player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("not_enough_ammo"));
				}
				shooter.releaseUsingItem();
				stopShooting(stack, serverLevel, shooter);
				return;
			}

			int elapsedTime = getUseDuration(stack, shooter) - remainingUseDuration;
			int delayBetweenShots = getDelayBetweenShots(stack);

			if (elapsedTime == 0 && serverLevel.getGameTime() - getShootTimestamp(stack) < delayBetweenShots) {
				return;
			}

			boolean canShoot = switch (gunProperties.shootBehavior()) {
				case INSTANT -> elapsedTime % delayBetweenShots == 0;
				case ON_FULL_CHARGE -> elapsedTime % delayBetweenShots == 0 && elapsedTime > 0;
				case ON_RELEASE_INSTANT, ON_RELEASE_WITH_FULL_CHARGE -> false;
			};

			if (canShoot) {
				shoot(serverLevel, shooter, shooter.getUsedItemHand(), stack);
				stack.set(ModDataComponents.GUN_SHOOT_TIMESTAMP, serverLevel.getGameTime());
			}
		}
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int remainingUseDuration) {

		if (level instanceof ServerLevel serverLevel && gunProperties.shootBehavior().isOnRelease()) {
			int elapsedTime = getUseDuration(stack, shooter) - remainingUseDuration;
			int delayBetweenShots = getDelayBetweenShots(stack);

			switch (gunProperties.shootBehavior()) {
				case ON_RELEASE_INSTANT -> {
					shoot(serverLevel, shooter, shooter.getUsedItemHand(), stack);
					stack.set(ModDataComponents.GUN_SHOOT_TIMESTAMP, serverLevel.getGameTime());
				}
				case ON_RELEASE_WITH_FULL_CHARGE -> {
					if (elapsedTime >= delayBetweenShots && serverLevel.getGameTime() - getShootTimestamp(stack) < delayBetweenShots) {
						shoot(serverLevel, shooter, shooter.getUsedItemHand(), stack);
						stack.set(ModDataComponents.GUN_SHOOT_TIMESTAMP, serverLevel.getGameTime());
					}
				}
				default -> {}
			}
		}

		setGunState(stack, GunState.NONE);

		if (shooter instanceof Player player) {
			player.awardStat(Stats.ITEM_USED.get(this));
		}
		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (!level.isClientSide() && level instanceof ServerLevel serverLevel && entity instanceof LivingEntity shooter) {
			if (getGunState(stack) != GunState.RELOADING) return;

			if (slot == EquipmentSlot.MAINHAND && canReload(stack, shooter)) {
				long elapsedTime = serverLevel.getGameTime() - getReloadStartTime(stack);
				if (elapsedTime < 0) return;

				onReloadTick(stack, serverLevel, shooter, elapsedTime);

				if (elapsedTime >= getReloadDurationTicks(stack)) {
					finishReload(stack, serverLevel, shooter);
				}
				return;
			}

			stopReload(stack, serverLevel, shooter);
		}
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		return VALID_ENCHANTMENTS.stream().anyMatch(enchantment::is) || super.supportsEnchantment(stack, enchantment);
	}

	@Override
	public AmmoSupplier getAmmoForReload(ItemStack stack, LivingEntity shooter) {
		return AmmoSupplier.fromInventory(shooter, this, stack);
	}

	@Override
	public int getDefaultProjectileRange() {
		return Math.round(gunProperties.horizontalDefaultRange());
	}

	@Override
	protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		appendGunStats(stack, tooltip);
		tooltip.accept(ComponentUtil.EMPTY_LINE);
		tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getDefaultKey(), TextComponentUtil.getActionText("reload")).withStyle(TextStyles.DARK_GRAY));
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return SharedConstants.TICKS_PER_MINUTE * 60;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}

}
