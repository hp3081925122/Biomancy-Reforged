package com.github.elenterius.biomancy.crafting;

import com.github.elenterius.biomancy.init.ModRecipes;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public final class AnyFoodIngredient implements ICustomIngredient {

	public static final AnyFoodIngredient INSTANCE = new AnyFoodIngredient();
	public static final MapCodec<AnyFoodIngredient> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<ByteBuf, AnyFoodIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private AnyFoodIngredient() {}

	public static Ingredient ingredient() {
		return INSTANCE.toVanilla();
	}

	private static boolean hasNutrition(@Nullable FoodProperties foodProperties) {
		return foodProperties != null && foodProperties.nutrition() > 0;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return hasNutrition(stack.getFoodProperties(null));
	}

	@Override
	public Stream<ItemStack> getItems() {
		return BuiltInRegistries.ITEM.stream()
				.map(ItemStack::new)
				.filter(stack -> !stack.isEmpty() && hasNutrition(stack.getFoodProperties(null)));
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return ModRecipes.FOOD_NUTRITION_INGREDIENT.get();
	}

}
