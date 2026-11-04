package com.github.elenterius.biomancy.crafting.state;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.github.elenterius.biomancy.crafting.recipe.ProcessingRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public abstract class RecipeCraftingStateData<T extends ProcessingRecipe> implements ContainerData {

	public static final String RECIPE_ID_KEY = "RecipeId";
	public static final String TIME_ELAPSED_KEY = "TimeElapsed";
	public static final String TIME_FOR_COMPLETION_KEY = "TimeForCompletion";
	public static final String NUTRIENTS_COST_KEY = "NutrientsCost";

	public static final int TIME_INDEX = 0;
	public static final int TIME_FOR_COMPLETION_INDEX = 1;
	public static final int NUTRIENTS_COST_INDEX = 2;

	public int timeElapsed;
	public int timeForCompletion;
	public int nutrientsCost;

	private CraftingState craftingState = CraftingState.NONE;
	protected @Nullable Identifier recipeId;

	public CraftingState getCraftingState() {
		return craftingState;
	}

	public void setCraftingState(CraftingState craftingState) {
		this.craftingState = craftingState;
		if (craftingState == CraftingState.NONE) {
			clear();
		}
	}

	public boolean isCraftingCanceled() {
		return craftingState == CraftingState.CANCELED;
	}

	public void cancelCrafting() {
		clear();
		craftingState = CraftingState.CANCELED;
	}

	protected abstract boolean isRecipeOfInstance(Recipe<?> recipe);

	@SuppressWarnings("unchecked")
	public Optional<RecipeHolder<T>> getCraftingGoalRecipe(Level level) {
		if (recipeId == null) return Optional.empty();
		if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();

		Optional<RecipeHolder<?>> optional = serverLevel.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, recipeId));
		if (optional.isPresent() && isRecipeOfInstance(optional.get().value())) {
			return Optional.of((RecipeHolder<T>) optional.get());
		}

		return Optional.empty();
	}

	public void setCraftingGoalRecipe(RecipeHolder<T> recipe, RecipeInput input) {
		recipeId = recipe.id().identifier();
		timeForCompletion = recipe.value().getCraftingTimeTicks(input);
		nutrientsCost = recipe.value().getCraftingCostNutrients(input);
		timeElapsed = 0;
	}

	public abstract int getFuelCost();

	public void clear() {
		craftingState = CraftingState.NONE;
		recipeId = null;
		timeElapsed = 0;
		timeForCompletion = 0;
		nutrientsCost = 0;
	}

	public void serialize(ValueOutput output) {
		CraftingState.toNBT(output, craftingState);
		if (recipeId != null) {
			output.putString(RECIPE_ID_KEY, recipeId.toString());
		}
		output.putInt(TIME_ELAPSED_KEY, timeElapsed);
		output.putInt(TIME_FOR_COMPLETION_KEY, timeForCompletion);
		output.putInt(NUTRIENTS_COST_KEY, nutrientsCost);
	}

	public void deserialize(ValueInput input) {
		craftingState = CraftingState.fromNBT(input);
		recipeId = input.getString(RECIPE_ID_KEY).map(Identifier::tryParse).orElse(null);
		timeElapsed = input.getIntOr(TIME_ELAPSED_KEY, 0);
		timeForCompletion = input.getIntOr(TIME_FOR_COMPLETION_KEY, 0);
		nutrientsCost = input.getIntOr(NUTRIENTS_COST_KEY, 0);
	}

	protected void validateIndex(int index) {
		if (index < 0 || index >= getCount()) throw new IndexOutOfBoundsException("Index out of bounds:" + index);
	}

	@Override
	public int get(int index) {
		validateIndex(index);
		if (index == TIME_INDEX) return timeElapsed;
		else if (index == TIME_FOR_COMPLETION_INDEX) return timeForCompletion;
		else if (index == NUTRIENTS_COST_INDEX) return nutrientsCost;
		return 0;
	}

	@Override
	public void set(int index, int value) {
		validateIndex(index);
		if (index == TIME_INDEX) timeElapsed = value;
		else if (index == TIME_FOR_COMPLETION_INDEX) timeForCompletion = value;
		else if (index == NUTRIENTS_COST_INDEX) nutrientsCost = value;
	}

	@Override
	public int getCount() {
		return 3;
	}

}
