package com.github.elenterius.biomancy.datagen.recipes.builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;

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

	AdvancementHolder build(ResourceLocation recipeId, ResourceLocation advancementId) {
		builder.parent(net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
				.rewards(AdvancementRewards.Builder.recipe(recipeId))
				.requirements(AdvancementRequirements.Strategy.OR);
		return builder.build(advancementId);
	}

}
