package com.github.elenterius.biomancy.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Collection;

public class BloomberryItem extends SimpleItem {

	public BloomberryItem(Properties properties) {
		super(properties);
	}

	private static void applyPotion(ServerLevel level, LivingEntity livingEntity, Potion potion) {
		for (MobEffectInstance effectInstance : potion.getEffects()) {
			if (effectInstance.getEffect().value().isInstantenous()) {
				effectInstance.getEffect().value().applyInstantenousEffect(level, livingEntity, livingEntity, livingEntity, effectInstance.getAmplifier(), 1);
			}
			else {
				livingEntity.addEffect(new MobEffectInstance(effectInstance));
			}
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);

		if (level instanceof ServerLevel serverLevel) {
			Collection<Potion> potions = BuiltInRegistries.POTION.stream().toList();
			potions.stream().skip(level.getRandom().nextInt(potions.size())).findFirst().ifPresent(potion -> applyPotion(serverLevel, livingEntity, potion));
		}

		return result;
	}
}
