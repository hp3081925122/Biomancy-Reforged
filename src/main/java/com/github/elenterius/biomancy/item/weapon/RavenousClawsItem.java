package com.github.elenterius.biomancy.item.weapon;

import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.livingtool.LivingToolState;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.init.ModDamageSources;
import com.github.elenterius.biomancy.init.ModParticleTypes;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.item.CrosshairProvider;
import com.github.elenterius.biomancy.item.ItemCharge;
import com.github.elenterius.biomancy.item.MeleeDamageSourceProviderItem;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.CombatUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.FormatUtil;
import com.github.elenterius.biomancy.util.MobUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.client.GeoRenderProvider;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animation.*;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animatable.manager.AnimatableManager;
import com.github.elenterius.biomancy.util.CombatUtil;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;

public class RavenousClawsItem extends LivingClawsItem implements GeoItem, ItemCharge, MeleeDamageSourceProviderItem, CrosshairProvider {
	protected static final Identifier BASE_ATTACK_KNOCKBACK_ID = BiomancyMod.rl("base_attack_knockback");
	private final Lazy<ItemAttributeModifiers> brokenAttributes;
	private final Lazy<ItemAttributeModifiers> dormantAttributes;
	private final Lazy<ItemAttributeModifiers> awakenedAttributes;
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public RavenousClawsItem(ToolMaterial material, float attackDamage, float attackSpeed, int maxNutrients, Properties properties) {
		super(material, 0, 0, 0, maxNutrients, properties);

		float attackSpeedModifier = (float) (attackSpeed - Attributes.ATTACK_SPEED.value().getDefaultValue());
		brokenAttributes = Lazy.of(() -> createDefaultAttributeModifiers(0, 0, -0.5f).build());
		dormantAttributes = Lazy.of(() -> createDefaultAttributeModifiers(-1 + attackDamage, attackSpeedModifier, 0).build());
		awakenedAttributes = Lazy.of(() -> createDefaultAttributeModifiers(-1 + attackDamage + 2.5f, attackSpeedModifier, 0.5f).build());
	}

	private static void playClawSwipeFX(LivingEntity attacker) {
		attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), ModSoundEvents.CLAWS_ATTACK_STRONG.get(), attacker.getSoundSource(), 1f, 1f + attacker.getRandom().nextFloat() * 0.5f);
		if (attacker.level() instanceof ServerLevel serverLevel) {
			double xOffset = -Mth.sin(attacker.getYRot() * Mth.DEG_TO_RAD);
			double zOffset = Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD);
			serverLevel.sendParticles(ModParticleTypes.BLOODY_CLAWS_ATTACK.get(), attacker.getX() + xOffset, attacker.getY(0.52f), attacker.getZ() + zOffset, 0, xOffset, 0, zOffset, 0);
		}
	}

	private static void playBloodExplosionFX(LivingEntity target) {
		if (target.level() instanceof ServerLevel serverLevel) {
			float w = target.getBbWidth() * 0.45f;
			double x = serverLevel.getRandom().nextGaussian() * w;
			double y = serverLevel.getRandom().nextGaussian() * 0.2d;
			double z = serverLevel.getRandom().nextGaussian() * w;
			serverLevel.sendParticles(ModParticleTypes.FALLING_BLOOD.get(), target.getX(), target.getY(0.5f), target.getZ(), 20, x, y, z, 0.25);
		}
	}

	@Override
	protected ItemAttributeModifiers.Builder createDefaultAttributeModifiers(float attackDamageModifier, float attackSpeedModifier, float attackRangeModifier) {
		return super.createDefaultAttributeModifiers(attackDamageModifier, attackSpeedModifier, attackRangeModifier)
				.add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(BASE_ATTACK_KNOCKBACK_ID, 0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.MAINHAND);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
		return switch (getLivingToolState(stack)) {
			case BROKEN -> brokenAttributes.get();
			case DORMANT -> dormantAttributes.get();
			case AWAKENED -> awakenedAttributes.get();
		};
	}

	@Override
	public boolean hasNutrients(ItemStack container) {
		return getNutrients(container) > getLivingToolActionCost(container, LivingToolState.AWAKENED, null);
	}

	@Override
	public KeyPressResult onClientKeyPress(ItemStack stack, Level level, Player player, EquipmentSlot slot, byte flags) {
		if (!hasCharge(stack)) {
			player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("not_enough_blood_charge"));
			player.playSound(ModSoundEvents.FLESHKIN_NO.get(), 1f, 1f + player.level().getRandom().nextFloat() * 0.4f);
			return KeyPressResult.fail();
		}

		return KeyPressResult.success(flags);
	}

	@Override
	public int getMaxCharge(ItemStack container) {
		return 50;
	}

	@Override
	public void onChargeChanged(ItemStack livingTool, int oldValue, int newValue) {
		if (newValue <= 0 && getLivingToolState(livingTool) == LivingToolState.AWAKENED) {
			setLivingToolState(livingTool, LivingToolState.DORMANT);
		}
	}

	@Override
	public void onNutrientsChanged(ItemStack livingTool, int oldValue, int newValue) {
		LivingToolState prevState = getLivingToolState(livingTool);
		LivingToolState state = prevState;

		if (newValue <= 0) {
			if (state != LivingToolState.BROKEN) setLivingToolState(livingTool, LivingToolState.BROKEN);
			return;
		}

		if (state == LivingToolState.BROKEN) {
			state = LivingToolState.DORMANT;
		}

		int maxCost = getLivingToolMaxActionCost(livingTool, state);
		if (newValue < maxCost && state == LivingToolState.DORMANT) state = LivingToolState.BROKEN;

		if (state != prevState) setLivingToolState(livingTool, state);
	}

	@Override
	public void updateLivingToolState(ItemStack livingTool, ServerLevel level, Player player) {
		GeoItem.getOrAssignId(livingTool, level);

		LivingToolState state = getLivingToolState(livingTool);
		boolean hasNutrients = hasNutrients(livingTool);

		if (!hasNutrients) {
			if (state != LivingToolState.BROKEN) {
				setLivingToolState(livingTool, LivingToolState.BROKEN);
				SoundUtil.Server.playItemSound(level, player, ModSoundEvents.FLESHKIN_BREAK.get());
			}
			return;
		}

		switch (state) {
			case BROKEN, AWAKENED -> {
				setLivingToolState(livingTool, LivingToolState.DORMANT);
				SoundUtil.Server.playItemSound(level, player, ModSoundEvents.FLESHKIN_BECOME_DORMANT.get());
			}
			case DORMANT -> {
				if (hasCharge(livingTool)) {
					setLivingToolState(livingTool, LivingToolState.AWAKENED);
					SoundUtil.Server.playItemSound(level, player, ModSoundEvents.FLESHKIN_BECOME_AWAKENED.get());
				}
			}
		}
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack livingTool, Slot slot, ClickAction action, Player player) {
		if (player.level() instanceof ServerLevel serverLevel) GeoItem.getOrAssignId(livingTool, serverLevel);
		return super.overrideStackedOnOther(livingTool, slot, action, player);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack livingTool, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		if (player.level() instanceof ServerLevel serverLevel) GeoItem.getOrAssignId(livingTool, serverLevel);
		return super.overrideOtherStackedOnMe(livingTool, other, slot, action, player, access);
	}

	@Override
	public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
		if (itemAbility == ItemAbilities.SWORD_SWEEP) return false;
		return super.canPerformAction(stack, itemAbility);
	}

	@Override
	public @Nullable DamageSource getMeleeDamageSource(ItemStack stack, Entity target, LivingEntity attacker, float attackStrengthScale) {
		if (attackStrengthScale <= 0.9f) return null;

		return switch (getLivingToolState(stack)) {
			case BROKEN -> null;
			case DORMANT, AWAKENED -> {
				DamageSource damageSource = ModDamageSources.slash(attacker.level(), attacker);
				if (CombatUtil.isInvulnerableTo(target, damageSource)) yield null;
				yield damageSource;
			}
		};
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.level().isClientSide()) return;

		LivingToolState livingToolState = getLivingToolState(stack);
		boolean isFullAttackStrength = !(attacker instanceof Player player) || player.getAttackStrengthScale(0.5f) >= 0.9f;
		boolean isNotCreativePlayer = !MobUtil.isCreativePlayer(attacker);

		switch (livingToolState) {
			case BROKEN -> {  }
			case DORMANT -> {
				if (isNotCreativePlayer) {
					consumeNutrients(stack, 1);
				}

				if (isFullAttackStrength) {
					playClawSwipeFX(attacker);
					if (attacker.getRandom().nextInt(12) == 0) {
						attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), ModSoundEvents.CLAWS_ATTACK_BLEED_PROC.get(), attacker.getSoundSource(), 1f, 1f);

						CombatUtil.applyBleedEffect(target, 20);
						if (isNotCreativePlayer) {
							consumeNutrients(stack, 1);
						}
					}

					target.invulnerableTime = 0;
				}

				if (target.isDeadOrDying()) {
					addCharge(stack, 5);
				}
			}
			case AWAKENED -> {
				if (isNotCreativePlayer) {
					consumeCharge(stack, 1);
				}

				if (isFullAttackStrength) {
					playClawSwipeFX(attacker);
					if (attacker.getRandom().nextInt(5) == 0) {
						attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), ModSoundEvents.CLAWS_ATTACK_BLEED_PROC.get(), attacker.getSoundSource(), 1f, 1f);

						if (CombatUtil.getBleedEffectLevel(target) < 2) {
							playBloodExplosionFX(target);
							CombatUtil.hurtWithBleed(target, 0.1f * target.getMaxHealth());

							if (isNotCreativePlayer) {
								consumeCharge(stack, 4);
							}
						}

						CombatUtil.applyBleedEffect(target, 20);
						if (isNotCreativePlayer) {
							consumeCharge(stack, 1);
						}
					}

					target.invulnerableTime = 0;
				}
			}
		}

	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
		tooltip.accept(ComponentUtil.EMPTY_LINE);

		appendLivingToolTooltip(stack, tooltip);

		if (stack.isEnchanted()) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);
		}
	}

	@Override
	public void appendLivingToolTooltip(ItemStack stack, Consumer<Component> tooltip) {
		LivingToolState livingToolState = getLivingToolState(stack);

		switch (livingToolState) {
			case BROKEN -> {
			}
			case DORMANT -> {
				tooltip.accept(TextComponentUtil.getAbilityText("bleed_proc").append(" (8% chance)").withStyle(ChatFormatting.GRAY));
				tooltip.accept(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("bleed_proc.desc")).withStyle(ChatFormatting.DARK_GRAY));
				tooltip.accept(ComponentUtil.EMPTY_LINE);
			}
			case AWAKENED -> {
				tooltip.accept(TextComponentUtil.getAbilityText("bleed_proc").append(" (20% chance)").withStyle(ChatFormatting.GRAY));
				tooltip.accept(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("bleed_proc.desc")).withStyle(ChatFormatting.DARK_GRAY));
				tooltip.accept(TextComponentUtil.getAbilityText("blood_explosion").append(" (20% chance)").withStyle(ChatFormatting.GRAY));
				tooltip.accept(ComponentUtil.literal(" ").append(TextComponentUtil.getAbilityText("blood_explosion.desc")).withStyle(ChatFormatting.DARK_GRAY));
				tooltip.accept(ComponentUtil.EMPTY_LINE);
			}
		}

		DecimalFormat df = FormatUtil.getIntegerFormatter();
		tooltip.accept(TextComponentUtil.getTooltipText("nutrients_fuel").withStyle(ChatFormatting.GRAY));
		tooltip.accept(ComponentUtil.literal(" %s/%s".formatted(df.format(getNutrients(stack)), df.format(getMaxNutrients(stack)))).withStyle(TextStyles.NUTRIENTS));
		tooltip.accept(TextComponentUtil.getTooltipText("blood_charge").withStyle(ChatFormatting.GRAY));
		tooltip.accept(ComponentUtil.literal(" %s/%s".formatted(df.format(getCharge(stack)), df.format(getMaxCharge(stack)))).withStyle(TextStyles.ERROR));

		switch (livingToolState) {
			case BROKEN -> {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				tooltip.accept(livingToolState.getTooltip());
			}
			case DORMANT -> {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				tooltip.accept(livingToolState.getTooltip().withStyle(TextStyles.ITALIC_GRAY));
				tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getDefaultKey(), TextComponentUtil.getActionText("enable_awakened_mode")));
			}
			case AWAKENED -> {
				tooltip.accept(ComponentUtil.EMPTY_LINE);
				tooltip.accept(livingToolState.getTooltip().withStyle(TextStyles.ITALIC_GRAY));
				tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getDefaultKey(), TextComponentUtil.getActionText("disable_awakened_mode")));
			}
		}
	}

	@Override
	public int getLivingToolActionCost(ItemStack livingTool, LivingToolState state, ItemAbility itemAbility) {
		return switch (state) {
			case AWAKENED, DORMANT -> 1;
			case BROKEN -> 0;
		};
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	private PlayState handleAnimationState(AnimationTest<RavenousClawsItem> animationState) {
		ItemStack stack = animationState.getData(ModDataTickets.ITEMSTACK);
		LivingToolState toolState = stack != null ? getLivingToolState(stack) : LivingToolState.BROKEN;

		AnimationController<RavenousClawsItem> controller = animationState.controller();
		switch (toolState) {
			case DORMANT -> Animations.setDormant(controller);
			case AWAKENED -> Animations.setAwakened(controller);
			case BROKEN -> Animations.setBroken(controller);
		}

		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(Animations.MAIN_CONTROLLER, 1, this::handleAnimationState));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	protected static final Identifier CROSSHAIR = BiomancyMod.rl("textures/gui/claws_crosshair.png");

	@Override
	public Identifier getCrosshairTexture(ItemStack stack, Player player) {
		return CROSSHAIR;
	}

	protected static class Animations {
		public static final String MAIN_CONTROLLER = "main";

		protected static final RawAnimation DORMANT = RawAnimation.begin().thenLoop("ravenous_claws.dormant");
		protected static final RawAnimation TO_SLEEP_TRANSITION = RawAnimation.begin().thenPlay("ravenous_claws.tosleep").thenLoop("ravenous_claws.dormant");
		protected static final RawAnimation BROKEN = RawAnimation.begin().thenLoop("ravenous_claws.broken");
		protected static final RawAnimation WAKEUP_TRANSITION = RawAnimation.begin().thenPlay("ravenous_claws.wakeup").thenLoop("ravenous_claws.awakened");
		protected static final RawAnimation AWAKENED = RawAnimation.begin().thenLoop("ravenous_claws.awakened");

		private Animations() {}

		protected static void setDormant(AnimationController<?> controller) {
			AnimationPoint current = controller.getCurrentAnimationPoint();
			if (current == null) {
				controller.setAnimation(DORMANT);
				return;
			}

			if (!current.animation().name().equals("ravenous_claws.dormant")) {
				controller.setAnimation(TO_SLEEP_TRANSITION);
				return;
			}

			controller.setAnimation(DORMANT);
		}

		protected static void setAwakened(AnimationController<?> controller) {
			AnimationPoint current = controller.getCurrentAnimationPoint();
			if (current == null) {
				controller.setAnimation(AWAKENED);
				return;
			}

			if (!current.animation().name().equals("ravenous_claws.awakened")) {
				controller.setAnimation(WAKEUP_TRANSITION);
				return;
			}

			controller.setAnimation(AWAKENED);
		}

		public static void setBroken(AnimationController<RavenousClawsItem> controller) {
			controller.setAnimation(BROKEN);
		}
	}

}
