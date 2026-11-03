package com.github.elenterius.biomancy.datagen.recipes;

import com.github.elenterius.biomancy.datagen.recipes.builder.FinishedRecipe;
import com.github.elenterius.biomancy.datagen.recipes.builder.WikiRecipe;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WikiRecipesProvider implements DataProvider {

	public static Set<RecipeSerializer<? extends Recipe<?>>> SUPPORTED_VANILLA_RECIPES = Set.of(
			RecipeSerializer.SMELTING_RECIPE, RecipeSerializer.BLASTING_RECIPE, RecipeSerializer.CAMPFIRE_COOKING_RECIPE,
			RecipeSerializer.SMOKING_RECIPE, RecipeSerializer.SHAPED_RECIPE, RecipeSerializer.SHAPELESS_RECIPE,
			RecipeSerializer.SMITHING_TRANSFORM, RecipeSerializer.STONECUTTER
	);

	private final ModRecipeProvider recipeProvider;
	private final CompletableFuture<HolderLookup.Provider> registries;
	private final Path basePath;

	public WikiRecipesProvider(PackOutput output, ModRecipeProvider recipeProvider, CompletableFuture<HolderLookup.Provider> registries) {
		this.recipeProvider = recipeProvider;
		this.registries = registries;
		basePath = output.getOutputFolder().resolve(".wiki_data");
	}

	public Path resolveJsonPath(ResourceLocation location) {
		return basePath.resolve(location.getNamespace()).resolve("recipe").resolve(location.getPath() + ".json");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return registries.thenCompose(lookup -> {
			DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			Set<ResourceLocation> set = Sets.newHashSet();
			List<CompletableFuture<?>> list = new ArrayList<>();

			recipeProvider.buildRecipes(finishedRecipe -> {
				if (!set.add(finishedRecipe.getId())) {
					throw new IllegalStateException("Duplicate recipe " + finishedRecipe.getId());
				}

				if (finishedRecipe instanceof WikiRecipe wikiRecipe) {
					list.add(DataProvider.saveStable(output, wikiRecipe.serializeWikiRecipe(ops), resolveJsonPath(finishedRecipe.getId())));
				}
				else if (SUPPORTED_VANILLA_RECIPES.contains(finishedRecipe.getType())) {
					list.add(DataProvider.saveStable(output, serializeTransformVanilla(finishedRecipe, ops), resolveJsonPath(finishedRecipe.getId())));
				}
			});

			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		});
	}

	private JsonObject serializeTransformVanilla(FinishedRecipe finishedRecipe, DynamicOps<JsonElement> ops) {
		JsonObject json = finishedRecipe.serializeRecipe(ops);

		RecipeSerializer<?> type = finishedRecipe.getType();

		if (SUPPORTED_VANILLA_RECIPES.contains(type)) {

			if (type != RecipeSerializer.STONECUTTER && type != RecipeSerializer.BLASTING_RECIPE) {
				if (json.has("ingredient")) {
					WikiRecipe.transform(json.get("ingredient"));
				}
			}

			if (json.has("result")) {
				WikiRecipe.transform(json.get("result"));
			}
		}

		return json;
	}

	@Override
	public String getName() {
		return "Wiki Recipe Provider";
	}

}
