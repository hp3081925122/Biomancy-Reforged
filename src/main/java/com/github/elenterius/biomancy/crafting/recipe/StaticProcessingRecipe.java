package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.world.item.crafting.RecipeInput;

public abstract non-sealed class StaticProcessingRecipe implements ProcessingRecipe {

	protected final int craftingTimeTicks;
	protected final int craftingCostNutrients;

	protected StaticProcessingRecipe(int craftingTimeTicks, int craftingCostNutrients) {
		this.craftingTimeTicks = craftingTimeTicks;
		this.craftingCostNutrients = craftingCostNutrients;
	}

	public final int getCraftingTimeTicks() {
		return craftingTimeTicks;
	}

	public final int getCraftingCostNutrients() {
		return craftingCostNutrients;
	}

	@Override
	public final int getCraftingTimeTicks(RecipeInput input) {
		return craftingTimeTicks;
	}

	@Override
	public final int getCraftingCostNutrients(RecipeInput input) {
		return craftingCostNutrients;
	}

}
