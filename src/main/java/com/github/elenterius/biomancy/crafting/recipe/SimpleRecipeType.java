package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class SimpleRecipeType<T extends Recipe<RecipeInput>> implements RecipeType<T> {

	private final String identifier;

	public SimpleRecipeType(String identifier) {
		this.identifier = identifier;
	}

	public String getId() {
		return identifier;
	}

	@Override
	public String toString() {
		return identifier;
	}

	public static class AdvancedRecipeType<R extends Recipe<RecipeInput>> extends SimpleRecipeType<R> {

		public AdvancedRecipeType(String identifier) {
			super(identifier);
		}

		private List<RecipeHolder<R>> getAllRecipes(Level level) {
			return level.getRecipeManager().getAllRecipesFor(this);
		}

		public Optional<RecipeHolder<R>> getRecipeById(Level level, ResourceLocation id) {
			return level.getRecipeManager().byKey(id).flatMap(this::castRecipe);
		}

		public Optional<RecipeHolder<R>> getFirstRecipeFor(Level level, RecipeInput input) {
			return level.getRecipeManager().getRecipeFor(this, input, level);
		}

		public Optional<RecipeHolder<R>> getBestRecipeFor(Level level, RecipeInput input) {
			RecipeHolder<R> topRecipe = null;
			int topPriority = Integer.MIN_VALUE;

			for (RecipeHolder<R> holder : getAllRecipes(level)) {
				if (!holder.value().matches(input, level)) continue;

				int currentPriority = RecipeWithMatchPriority.getOrComputeMatchPriority(holder.value());
				if (currentPriority > topPriority) {
					topRecipe = holder;
					topPriority = currentPriority;
				}
			}

			return Optional.ofNullable(topRecipe);
		}

		@SuppressWarnings("unchecked")
		private Optional<RecipeHolder<R>> castRecipe(RecipeHolder<?> holder) {
			if (holder.value().getType() != this) return Optional.empty();
			return Optional.of((RecipeHolder<R>) holder);
		}

		private boolean matches(R recipe, ItemStack stack) {
			for (Ingredient ingredient : recipe.getIngredients()) {
				if (ingredient.test(stack)) return true;
			}
			return false;
		}

		public Optional<RecipeHolder<R>> getFirstRecipeForIngredient(Level level, ItemStack stack) {
			return getAllRecipes(level).stream()
					.filter(holder -> matches(holder.value(), stack))
					.findFirst();
		}

		public Optional<RecipeHolder<R>> getBestRecipeForIngredient(Level level, ItemStack stack) {
			return getBestRecipeForIngredient(level, stack, null);
		}

		public Optional<RecipeHolder<R>> getFirstRecipeForIngredient(Level level, ItemStack stack, @Nullable ResourceLocation lastRecipeId) {
			if (lastRecipeId != null) {
				Optional<RecipeHolder<R>> last = getRecipeById(level, lastRecipeId).filter(holder -> matches(holder.value(), stack));
				if (last.isPresent()) return last;
			}

			return getFirstRecipeForIngredient(level, stack);
		}

		public Optional<RecipeHolder<R>> getBestRecipeForIngredient(Level level, ItemStack stack, @Nullable ResourceLocation lastRecipeId) {
			if (lastRecipeId != null) {
				Optional<RecipeHolder<R>> last = getRecipeById(level, lastRecipeId).filter(holder -> matches(holder.value(), stack));
				if (last.isPresent()) return last;
			}

			RecipeHolder<R> topRecipe = null;
			int topPriority = Integer.MIN_VALUE;

			for (RecipeHolder<R> holder : getAllRecipes(level)) {
				if (!matches(holder.value(), stack)) continue;

				int currentPriority = RecipeWithMatchPriority.getOrComputeMatchPriority(holder.value());
				if (currentPriority > topPriority) {
					topRecipe = holder;
					topPriority = currentPriority;
				}
			}

			return Optional.ofNullable(topRecipe);
		}

	}

}
