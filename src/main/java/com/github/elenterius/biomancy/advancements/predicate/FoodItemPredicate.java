package com.github.elenterius.biomancy.advancements.predicate;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class FoodItemPredicate implements ItemSubPredicate {

	public static final FoodItemPredicate INSTANCE = new FoodItemPredicate();
	public static final ItemSubPredicate.Type<FoodItemPredicate> TYPE = new ItemSubPredicate.Type<>(MapCodec.unit(INSTANCE).codec());

	private FoodItemPredicate() {}

	@Override
	public boolean matches(ItemStack stack) {
		if (!stack.has(DataComponents.FOOD)) return false;
		return stack.getFoodProperties(null) != null;
	}

}
