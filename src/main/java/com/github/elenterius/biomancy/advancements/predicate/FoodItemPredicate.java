package com.github.elenterius.biomancy.advancements.predicate;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public final class FoodItemPredicate implements DataComponentPredicate {

	public static final FoodItemPredicate INSTANCE = new FoodItemPredicate();
	public static final DataComponentPredicate.Type<FoodItemPredicate> TYPE = new DataComponentPredicate.ConcreteType<>(MapCodec.unitCodec(INSTANCE));

	private FoodItemPredicate() {}

	@Override
	public boolean matches(DataComponentGetter components) {
		return components.get(DataComponents.FOOD) != null;
	}

}
