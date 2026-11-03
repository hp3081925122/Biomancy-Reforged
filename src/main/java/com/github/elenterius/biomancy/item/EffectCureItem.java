package com.github.elenterius.biomancy.item;

import net.neoforged.neoforge.common.EffectCure;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EffectCureItem extends SimpleItem {

	public EffectCureItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide) entity.removeEffectsCuredBy(EffectCure.get(BuiltInRegistries.ITEM.getKey(this).toString()));
		return entity.eat(level, stack);
	}

}
