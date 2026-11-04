package com.github.elenterius.biomancy.serum;

import net.minecraft.core.component.DataComponents;
import java.util.function.Consumer;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.ArrayList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.entity.mob.ai.goal.FrenzyAttackableTargetGoal;
import com.github.elenterius.biomancy.entity.mob.ai.goal.FrenzyMeleeAttackGoal;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FrenzySerum extends BasicSerum {

	public static final int DEFAULT_DURATION_TICKS = 20 * (2 * 60 + 30); // 2 min : 30 sec
	public static final int MIN_DURATION_TICKS = 20 * 30; // 30 sec
	public static final double ATTACK_DAMAGE_FALLBACK = 1;

	public FrenzySerum(int colorIn) {
		super(colorIn);
	}

	@Override
	public void affectEntity(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		if (target instanceof Rabbit rabbit && rabbit.getVariant() != Rabbit.Variant.EVIL) {
			rabbit.setComponent(DataComponents.RABBIT_VARIANT, Rabbit.Variant.EVIL);
			return;
		}

		addStatusEffect(target);
	}

	@Override
	public void affectPlayerSelf(ServerLevel level, CompoundTag tag, ServerPlayer targetSelf) {
		addStatusEffect(targetSelf);
	}

	private void addStatusEffect(LivingEntity target) {
		target.addEffect(new MobEffectInstance(ModMobEffects.FRENZY, DEFAULT_DURATION_TICKS, 0));
	}

	@Override
	public void appendTooltip(CompoundTag tag, @Nullable Level level, Consumer<Component> tooltip, TooltipFlag flagIn) {
		tooltip.accept(ComponentUtil.translatable(getDescriptionTranslationKey()).withStyle(TextStyles.LORE));
		tooltip.accept(ComponentUtil.EMPTY_LINE);
		float tickRate = level != null ? level.tickRateManager().tickrate() : 20f;
		addEffectToClientTooltip(tooltip, ModMobEffects.FRENZY, 0, DEFAULT_DURATION_TICKS, tickRate);
	}

	public void addEffectToClientTooltip(Consumer<Component> tooltip, Holder<MobEffect> effectHolder, int amplifier, int duration, float tickRate) {
		MobEffect effect = effectHolder.value();
		MutableComponent effectText = ComponentUtil.translatable(effect.getDescriptionId());
		if (amplifier > 0) effectText = ComponentUtil.translatable("potion.withAmplifier", effectText, ComponentUtil.translatable("potion.potency." + amplifier));
		if (duration > 20) effectText = ComponentUtil.translatable("potion.withDuration", effectText, StringUtil.formatTickDuration(duration, tickRate));
		tooltip.accept(effectText.withStyle(effect.getCategory().getTooltipFormatting()));

		List<Pair<Holder<Attribute>, AttributeModifier>> effectModifiers = new ArrayList<>();
		effect.createModifiers(amplifier, (attribute, modifier) -> effectModifiers.add(Pair.of(attribute, modifier)));
		if (!effectModifiers.isEmpty()) {
			tooltip.accept(ComponentUtil.EMPTY_LINE);

			for (Pair<Holder<Attribute>, AttributeModifier> entry : effectModifiers) {
				AttributeModifier modifier = entry.getSecond();
				AttributeModifier.Operation operation = modifier.operation();
				double value = modifier.amount();
				double amount = operation != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && operation != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? value : value * 100d;

				MutableComponent attributeText = ComponentUtil.translatable(entry.getFirst().value().getDescriptionId());
				if (value > 0) {
					tooltip.accept((ComponentUtil.translatable("attribute.modifier.plus." + operation.id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(amount), attributeText)).withStyle(ChatFormatting.BLUE));
				}
				else if (value < 0) {
					amount = amount * -1d;
					tooltip.accept((ComponentUtil.translatable("attribute.modifier.take." + operation.id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(amount), attributeText)).withStyle(ChatFormatting.RED));
				}
			}
		}
	}

	public static void injectAIBehavior(Mob mob) {
		if (!(mob instanceof PathfinderMob) && !(mob instanceof RangedAttackMob)) return;

		if (!hasFrenzyTargetGoal(mob)) {
			mob.targetSelector.addGoal(1, new FrenzyAttackableTargetGoal<>(mob, LivingEntity.class));
		}

		if (!(mob instanceof RangedAttackMob) && !(mob instanceof Enemy) && mob instanceof PathfinderMob pathfinderMob) {
			if (!hasMeleeAttackGoal(mob)) {
				mob.goalSelector.addGoal(4, new FrenzyMeleeAttackGoal(pathfinderMob, 1d, false));
			}
		}
	}

	private static boolean hasMeleeAttackGoal(Mob mob) {
		for (WrappedGoal availableGoal : mob.goalSelector.getAvailableGoals()) {
			if (availableGoal.getGoal() instanceof MeleeAttackGoal) return true;
		}
		return false;
	}

	private static boolean hasFrenzyTargetGoal(Mob mob) {
		for (WrappedGoal availableGoal : mob.targetSelector.getAvailableGoals()) {
			if (availableGoal.getGoal() instanceof FrenzyAttackableTargetGoal) return true;
		}
		return false;
	}

}
