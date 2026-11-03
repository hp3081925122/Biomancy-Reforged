package com.github.elenterius.biomancy.statuseffect;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class StatusEffect extends MobEffect {

	protected final boolean isCurable;

	public StatusEffect(MobEffectCategory category, int color) {
		this(category, color, true);
	}

	public StatusEffect(MobEffectCategory category, int color, boolean isCurable) {
		super(category, color);
		this.isCurable = isCurable;
	}

	@SuppressWarnings("unchecked")
	public <E extends StatusEffect> E addModifier(Holder<Attribute> attribute, String id, double amount, AttributeModifier.Operation operation) {
		return (E) addAttributeModifier(attribute, BiomancyMod.rl(id), amount, operation);
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
		return true;
	}

	@Override
	public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity livingEntity, int amplifier, double health) {
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return false;
	}

	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
		if (isCurable) {
			super.fillEffectCures(cures, effectInstance);
		}
		else {
			cures.add(EffectCures.PROTECTED_BY_TOTEM);
		}
	}

}
