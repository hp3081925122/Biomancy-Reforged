package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public interface RecipeWithMatchPriority extends Recipe<RecipeInput> {

	int getMatchPriority();

	List<Ingredient> getIngredients();

	@Override
	default boolean showNotification() {
		return true;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	static List<Ingredient> getIngredientsOf(Recipe<?> recipe) {
		if (recipe instanceof RecipeWithMatchPriority r) return r.getIngredients();
		if (recipe instanceof BioForgingRecipe r) return r.getIngredients();
		return recipe.placementInfo().ingredients();
	}

	static int getOrComputeMatchPriority(Recipe<?> recipe) {
		if (recipe instanceof RecipeWithMatchPriority r) return r.getMatchPriority();
		return computeMatchPriority(getIngredientsOf(recipe));
	}

	static int computeMatchPriority(List<Ingredient> ingredients) {
		int bias = 0;
		for (Ingredient ingredient : ingredients) {
			bias += computeItemBias(ingredient);
		}
		return bias;
	}

	private static int computeItemBias(Ingredient ingredient) {
		if (ingredient.isCustom()) return -2;

		HolderSet<Item> values = ingredient.getValues();
		if (values.unwrapKey().isPresent()) return -1;
		return values.size() * 2;
	}

}
