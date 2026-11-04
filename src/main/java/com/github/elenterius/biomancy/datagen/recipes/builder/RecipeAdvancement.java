package com.github.elenterius.biomancy.datagen.recipes.builder;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.resources.Identifier;

final class RecipeAdvancement {

	private final Advancement.Builder builder = Advancement.Builder.recipeAdvancement();
	private int criteriaCount = 0;

	void addCriterion(String name, Criterion<?> criterion) {
		builder.addCriterion(name, criterion);
		criteriaCount++;
	}

	boolean hasNoCriteria() {
		return criteriaCount == 0;
	}

	AdvancementHolder build(Identifier recipeId, Identifier advancementId) {
		ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
		builder.parent(net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeKey))
				.rewards(AdvancementRewards.Builder.recipe(recipeKey))
				.requirements(AdvancementRequirements.Strategy.OR);
		return builder.build(advancementId);
	}

}
