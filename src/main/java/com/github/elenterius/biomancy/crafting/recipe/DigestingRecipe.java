package com.github.elenterius.biomancy.crafting.recipe;

import com.github.elenterius.biomancy.init.ModRecipes;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

public non-sealed interface DigestingRecipe extends ProcessingRecipe {
	Ingredient getIngredient();

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return ModRecipes.DIGESTING_CATEGORY.get();
	}
}
