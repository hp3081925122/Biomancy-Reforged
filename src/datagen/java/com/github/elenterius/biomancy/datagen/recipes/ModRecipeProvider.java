package com.github.elenterius.biomancy.datagen.recipes;

import com.github.elenterius.biomancy.datagen.recipes.builder.FinishedRecipe;
import com.github.elenterius.biomancy.datagen.recipes.builder.RecipeJson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModRecipeProvider implements DataProvider {

	private final PackOutput.PathProvider recipePathProvider;
	private final PackOutput.PathProvider advancementPathProvider;
	private final CompletableFuture<HolderLookup.Provider> registries;

	private final DecomposingRecipeProvider decomposingRecipeProvider;
	private final DigestingRecipeProvider digestingRecipeProvider;
	private final BioForgingRecipeProvider bioforgingRecipeProvider;
	private final BioBrewingRecipeProvider biobrewingRecipeProvider;
	private final VanillaRecipeProvider vanillaRecipeProvider;

	public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		this.registries = registries;
		recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
		advancementPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
		decomposingRecipeProvider = new DecomposingRecipeProvider();
		digestingRecipeProvider = new DigestingRecipeProvider();
		bioforgingRecipeProvider = new BioForgingRecipeProvider();
		biobrewingRecipeProvider = new BioBrewingRecipeProvider();
		vanillaRecipeProvider = new VanillaRecipeProvider();
	}

	public void buildRecipes(Consumer<FinishedRecipe> consumer) {
		decomposingRecipeProvider.buildRecipes(consumer);
		digestingRecipeProvider.buildRecipes(consumer);
		bioforgingRecipeProvider.buildRecipes(consumer);
		biobrewingRecipeProvider.buildRecipes(consumer);
		vanillaRecipeProvider.buildRecipes(consumer);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return registries.thenCompose(lookup -> {
			DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
			Set<Identifier> recipeIds = new HashSet<>();
			List<CompletableFuture<?>> futures = new ArrayList<>();

			buildRecipes(recipe -> {
				if (!recipeIds.add(recipe.getId())) {
					throw new IllegalStateException("Duplicate recipe " + recipe.getId());
				}

				futures.add(DataProvider.saveStable(output, recipe.serializeRecipe(ops), recipePathProvider.json(recipe.getId())));

				AdvancementHolder advancement = recipe.getAdvancement();
				JsonObject advancementJson = RecipeJson.advancement(advancement, recipe.getConditions(), ops);
				if (advancementJson != null) {
					futures.add(DataProvider.saveStable(output, advancementJson, advancementPathProvider.json(advancement.id())));
				}
			});

			return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		});
	}

	@Override
	public String getName() {
		return "Biomancy Recipes";
	}

}
