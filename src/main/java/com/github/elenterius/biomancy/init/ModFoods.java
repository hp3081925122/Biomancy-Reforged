package com.github.elenterius.biomancy.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public final class ModFoods {

	private static final float FAST_CONSUME_SECONDS = Consumable.DEFAULT_CONSUME_SECONDS / 2f;

	public static final Food VOLATILE_GLAND = new Food(
			new FoodProperties.Builder().nutrition(2).saturationModifier(0.8f).alwaysEdible().build(),
			Consumable.builder().consumeSeconds(FAST_CONSUME_SECONDS).build()
	);

	public static final Food TOXIN_GLAND = new Food(
			new FoodProperties.Builder().nutrition(2).saturationModifier(0.8f).alwaysEdible().build(),
			Consumable.builder()
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 1400, 1), 1f))
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 150, 0), 0.8f))
					.build()
	);

	public static final Food CORROSIVE_FLUID = new Food(
			new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build(),
			Consumable.builder()
					.animation(ItemUseAnimation.DRINK).sound(ModSoundEvents.MARROW_DRINK)
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModMobEffects.CORROSIVE, 20 * 2, 0), 1f))
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModMobEffects.ARMOR_SHRED, 20 * 5, 0), 1f))
					.build()
	);

	public static final Food GASTRIC_JUICE = new Food(
			new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build(),
			Consumable.builder()
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModMobEffects.CORROSIVE, 20 * 4, 0), 1f))
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModMobEffects.ARMOR_SHRED, 20 * (4 + 3), 0), 1f))
					.build()
	);

	public static final Food MARROW_FLUID = new Food(
			new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build(),
			Consumable.builder().animation(ItemUseAnimation.DRINK).sound(ModSoundEvents.MARROW_DRINK).build()
	);

	public static final Food POOR_FLESH = simple(1, 0.2f);

	public static final Food LIVING_FLESH = new Food(
			new FoodProperties.Builder().nutrition(2).saturationModifier(0.4f).build(),
			Consumable.builder()
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModMobEffects.PRIMORDIAL_INFESTATION, 20 * 20, 0), 0.4f))
					.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 6, 0), 0.8f))
					.build()
	);

	public static final Food NUTRIENT_BAR = simple(9, 1.2f);
	public static final Food NUTRIENT_PASTE = simple(1, 0.6f);

	private ModFoods() {}

	private static Food simple(int nutrition, float saturationModifier) {
		return new Food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build(), Consumable.builder().build());
	}

	public record Food(FoodProperties properties, Consumable consumable) {

		public Item.Properties apply(Item.Properties itemProperties) {
			return itemProperties.food(properties, consumable);
		}

	}

}
