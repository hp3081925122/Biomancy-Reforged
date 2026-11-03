package com.github.elenterius.biomancy.integration.jei;

import com.github.elenterius.biomancy.block.digester.DigesterBlockEntity;
import com.github.elenterius.biomancy.crafting.recipe.DigestingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.FoodDigestingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.StaticDigestingRecipe;
import com.github.elenterius.biomancy.init.ModRecipes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;

public final class DigestingRecipes {

	private DigestingRecipes() {}

	public static List<DigestingRecipe> getRecipes(ClientLevel level) {
		List<RecipeHolder<DigestingRecipe>> allRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.DIGESTING_RECIPE_TYPE.get());

		List<DigestingRecipe> resolvedRecipes = new ArrayList<>();

		for (RecipeHolder<DigestingRecipe> holder : allRecipes) {
			if (holder.value() instanceof FoodDigestingRecipe dynamicRecipe) {
				resolvedRecipes.addAll(convertToStaticRecipes(level, dynamicRecipe));
			}
			else {
				resolvedRecipes.add(holder.value());
			}
		}

		return resolvedRecipes;
	}

	private static List<DigestingRecipe> convertToStaticRecipes(ClientLevel level, FoodDigestingRecipe dynamicRecipe) {
		List<DigestingRecipe> staticRecipes = new ArrayList<>();

		ItemStackHandler inventory = new ItemStackHandler(DigesterBlockEntity.INPUT_SLOTS);
		RecipeWrapper inputInventory = new RecipeWrapper(inventory);

		for (ItemStack ingredientItem : dynamicRecipe.getIngredient().getItems()) {
			inventory.setStackInSlot(0, ingredientItem);

			ItemStack result = dynamicRecipe.assemble(inputInventory, level.registryAccess());
			int craftingTimeTicks = dynamicRecipe.getCraftingTimeTicks(inputInventory);
			int craftingCostNutrients = dynamicRecipe.getCraftingCostNutrients(inputInventory);
			Ingredient ingredient = Ingredient.of(ingredientItem);

			staticRecipes.add(new StaticDigestingRecipe(result, craftingTimeTicks, craftingCostNutrients, ingredient));
		}

		return staticRecipes;
	}

}
