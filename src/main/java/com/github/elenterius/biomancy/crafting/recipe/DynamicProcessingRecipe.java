package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public abstract non-sealed class DynamicProcessingRecipe implements ProcessingRecipe {

	private final RecipeType<? extends Recipe<RecipeInput>> type;

	protected DynamicProcessingRecipe(RecipeType<? extends Recipe<RecipeInput>> type) {
		this.type = type;
	}

	@Override
	public final boolean isSpecial() {
		return true;
	}

	public final ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public final RecipeType<? extends Recipe<RecipeInput>> getType() {
		return type;
	}

}
