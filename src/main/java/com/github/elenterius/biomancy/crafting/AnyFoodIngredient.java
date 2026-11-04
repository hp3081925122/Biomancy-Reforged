package com.github.elenterius.biomancy.crafting;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
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
		return hasNutrition(stack.get(DataComponents.FOOD));
	}

	@Override
	public Stream<Holder<Item>> items() {
		return BuiltInRegistries.ITEM.listElements()
				.filter(holder -> holder.value() != Items.AIR && hasNutrition(holder.value().components().get(DataComponents.FOOD)))
				.map(holder -> holder);
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
