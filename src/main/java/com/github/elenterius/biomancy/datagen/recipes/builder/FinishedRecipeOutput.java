package com.github.elenterius.biomancy.datagen.recipes.builder;

import net.minecraft.resources.ResourceKey;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public record FinishedRecipeOutput(Consumer<FinishedRecipe> consumer) implements RecipeOutput {

	public static RecipeOutput of(Consumer<FinishedRecipe> consumer) {
		return new FinishedRecipeOutput(consumer);
	}

	@Override
	public Advancement.Builder advancement() {
		return Advancement.Builder.recipeAdvancement().parent(net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
	}

	@Override
	public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
		consumer.accept(new VanillaRecipe(key.identifier(), recipe, advancement, List.of(conditions)));
	}

	@Override
	public void includeRootAdvancement() {
	}

	public record VanillaRecipe(Identifier id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, List<ICondition> conditions) implements FinishedRecipe {

		@Override
		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return recipe.getSerializer();
		}

		@Override
		public List<ICondition> getConditions() {
			return conditions;
		}

		@Override
		public @Nullable AdvancementHolder getAdvancement() {
			return advancement;
		}

		@Override
		public void serializeRecipeData(JsonObject json, DynamicOps<JsonElement> ops) {
			JsonObject encoded = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow().getAsJsonObject();
			encoded.entrySet().forEach(entry -> {
				if (!entry.getKey().equals("type")) json.add(entry.getKey(), entry.getValue());
			});
		}

	}

}
