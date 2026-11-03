package com.github.elenterius.biomancy.integration.pehkui;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import virtuoel.pehkui.api.*;

import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class PehkuiIntegration {

	private static final ResourceLocation HEALTH_MODIFIER_ID = BiomancyMod.rl("scaled_mob_health");
	private static final float DEFAULT_SCALE = 1f;

	private PehkuiIntegration() {}

	public static void init(Consumer<PehkuiHelper> helperSetter) {
		ScaleTypes.WIDTH.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.HEIGHT.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.MOTION.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.VISIBILITY.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.REACH.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.EXPLOSIONS.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);

		ScaleTypes.ATTACK.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.DEFENSE.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);
		ScaleTypes.KNOCKBACK.getDefaultBaseValueModifiers().add(SCALE_MODIFIER);

		PehkuiIntegration.SCALE_TYPE.getScaleChangedEvent().add(scaleData -> {
			if (scaleData.getEntity() instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
				float scale = scaleData.getScale();
				float targetScale = scaleData.getTargetScale();
				if (Mth.equal(targetScale, scale)) {
					onScaleChangeCompleted(livingEntity, targetScale);
				}
			}
		});

		helperSetter.accept(new PehkuiHelperImpl());
	}

	private static void onScaleChangeCompleted(LivingEntity livingEntity, float targetScale) {
		updateMaxHealth(livingEntity, targetScale);
	}

	private static void updateMaxHealth(LivingEntity livingEntity, float targetScale) {
		AttributeInstance healthAttribute = livingEntity.getAttribute(Attributes.MAX_HEALTH);
		if (healthAttribute != null) {
			healthAttribute.removeModifier(HEALTH_MODIFIER_ID);

			float modifierAmount = targetScale - DEFAULT_SCALE;
			if (modifierAmount != 0f) {
				healthAttribute.addTransientModifier(new AttributeModifier(HEALTH_MODIFIER_ID, modifierAmount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

				float maxHealth = livingEntity.getMaxHealth();
				if (livingEntity.getHealth() > maxHealth) {
					livingEntity.setHealth(maxHealth);
				}
			}
		}
	}

	private static ScaleModifier registerScaleModifier(String name, Supplier<ScaleModifier> factory) {
		return ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, BiomancyMod.rl(name), factory.get());
	}

	private static ScaleType registerScaleType(String name, UnaryOperator<ScaleType.Builder> builder) {
		return ScaleRegistries.register(ScaleRegistries.SCALE_TYPES, BiomancyMod.rl(name), builder.apply(ScaleType.Builder.create()).build());
	}

	static final class PehkuiHelperImpl implements PehkuiHelper {

		@Override
		public void setScale(LivingEntity livingEntity, float scale) {
			PehkuiIntegration.SCALE_TYPE.getScaleData(livingEntity).setTargetScale(scale);
		}

		@Override
		public void resetSize(LivingEntity livingEntity) {
			setScale(livingEntity, DEFAULT_SCALE);
		}

		@Override
		public void resize(LivingEntity livingEntity, float multiplier) {
			ScaleData scaleData = PehkuiIntegration.SCALE_TYPE.getScaleData(livingEntity);
			float targetScale = scaleData.getScale() * multiplier;
			scaleData.setTargetScale(targetScale);
		}

		@Override
		public float getScale(LivingEntity livingEntity) {
			ScaleData scaleData = PehkuiIntegration.SCALE_TYPE.getScaleData(livingEntity);
			return scaleData.getScale();
		}

	}

	private static final ScaleModifier SCALE_MODIFIER = registerScaleModifier("scale", () -> new TypedScaleModifier(() -> PehkuiIntegration.SCALE_TYPE));

	private static final ScaleType SCALE_TYPE = registerScaleType("size", builder -> builder.affectsDimensions().addDependentModifier(SCALE_MODIFIER));

}
