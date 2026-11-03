package com.github.elenterius.biomancy.statuseffect;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AttackDamageEffect extends StatusEffect {

	protected double damageMultiplier;

	public AttackDamageEffect(MobEffectCategory category, int color) {
		this(category, color, true);
	}

	public AttackDamageEffect(MobEffectCategory category, int color, boolean isCurable) {
		super(category, color, isCurable);
	}

	public AttackDamageEffect addAttackDamageModifier(String id, double damageMultiplier, AttributeModifier.Operation operation) {
		this.damageMultiplier = damageMultiplier;
		addAttributeModifier(Attributes.ATTACK_DAMAGE, BiomancyMod.rl(id), operation, amplifier -> (amplifier + 1) * this.damageMultiplier);
		return this;
	}

}
