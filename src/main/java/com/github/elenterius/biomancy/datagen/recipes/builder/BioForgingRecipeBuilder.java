package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.recipe.BioForgingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.RecipeUtil;
import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BioForgingRecipeBuilder implements RecipeBuilder<BioForgingRecipeBuilder> {

	public static final String RECIPE_SUB_FOLDER = ModRecipes.BIO_FORGING_RECIPE_TYPE.getId().getPath();

	private final ResourceLocation recipeId;

	private final List<ICondition> conditions = new ArrayList<>();
	private final ItemData result;
	private final List<IngredientStack> ingredients = new ArrayList<>();
	private final RecipeAdvancement advancement = new RecipeAdvancement();
	private BioForgeTab category = ModBioForgeTabs.MISC.get();

	private int craftingCostNutrients = -1;

	private BioForgingRecipeBuilder(ResourceLocation recipeId, ItemData result) {
		this.recipeId = ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), RECIPE_SUB_FOLDER + "/" + recipeId.getPath());
		this.result = result;
	}

	public static BioForgingRecipeBuilder create(ResourceLocation recipeId, ItemData result) {
		return new BioForgingRecipeBuilder(recipeId, result);
	}

	public static BioForgingRecipeBuilder create(String modId, String outputName, ItemData result) {
		ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modId, outputName);
		return new BioForgingRecipeBuilder(rl, result);
	}

	public static BioForgingRecipeBuilder create(String outputName, ItemData result) {
		ResourceLocation rl = BiomancyMod.rl(outputName);
		return new BioForgingRecipeBuilder(rl, result);
	}

	public static BioForgingRecipeBuilder create(ItemData result) {
		ResourceLocation rl = BiomancyMod.rl(result.getItemPath());
		return new BioForgingRecipeBuilder(rl, result);
	}

	public static BioForgingRecipeBuilder create(ItemStack stack) {
		return create(new ItemData(stack));
	}

	public static BioForgingRecipeBuilder create(ItemLike item) {
		return create(new ItemData(item));
	}

	public static BioForgingRecipeBuilder create(ItemLike item, int count) {
		return create(new ItemData(item, count));
	}

	//	public BioForgeRecipeBuilder setCraftingTime(int time) {
	//		if (time < 0) throw new IllegalArgumentException("Invalid crafting time: " + time);
	//		craftingTime = time;
	//		return this;
	//	}

	public BioForgingRecipeBuilder setCraftingCost(int costNutrients) {
		if (costNutrients < 0) throw new IllegalArgumentException("Invalid crafting cost: " + costNutrients);
		craftingCostNutrients = costNutrients;
		return this;
	}

	public BioForgingRecipeBuilder ifModLoaded(String modId) {
		return withCondition(new ModLoadedCondition(modId));
	}

	public BioForgingRecipeBuilder ifModMissing(String modId) {
		return withCondition(new NotCondition(new ModLoadedCondition(modId)));
	}

	public BioForgingRecipeBuilder withCondition(ICondition condition) {
		conditions.add(condition);
		return this;
	}

	public BioForgingRecipeBuilder setCategory(BioForgeTab category) {
		this.category = category;
		return this;
	}

	public BioForgingRecipeBuilder setCategory(Supplier<BioForgeTab> category) {
		this.category = category.get();
		return this;
	}

	public BioForgingRecipeBuilder addIngredient(TagKey<Item> tag) {
		return addIngredient(Ingredient.of(tag));
	}

	public BioForgingRecipeBuilder addIngredient(TagKey<Item> tag, int quantity) {
		return addIngredient(Ingredient.of(tag), quantity);
	}

	public BioForgingRecipeBuilder addIngredient(ItemLike item) {
		return addIngredient(item, 1);
	}

	public BioForgingRecipeBuilder addIngredient(Ingredient ingredient) {
		return addIngredient(ingredient, 1);
	}

	public BioForgingRecipeBuilder addIngredient(ItemLike item, int quantity) {
		addIngredient(Ingredient.of(item), quantity);
		return this;
	}

	public BioForgingRecipeBuilder addIngredient(Ingredient ingredient, int quantity) {
		ingredients.add(new IngredientStack(ingredient, quantity));
		return this;
	}

	@Override
	public BioForgingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
		advancement.addCriterion(name, criterion);
		return this;
	}

	@Override
	public void save(Consumer<FinishedRecipe> consumer, @Nullable RecipeCategory category) {
		validateCriteria();

		if (craftingCostNutrients < 0) {
			craftingCostNutrients = BioForgingRecipe.DEFAULT_CRAFTING_COST_NUTRIENTS;
		}

		String folderName = RecipeBuilder.getRecipeFolderName(category, BiomancyMod.MOD_ID);
		ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), "recipes/%s/%s".formatted(folderName, recipeId.getPath()));

		consumer.accept(new Result(this, advancement.build(recipeId, advancementId)));
	}

	private void validateCriteria() {
		if (advancement.hasNoCriteria()) {
			throw new IllegalStateException("No way of obtaining recipe %s because Criteria are empty.".formatted(recipeId));
		}
	}

	public static class Result implements FinishedRecipe, WikiRecipe {

		private final ResourceLocation id;
		private final List<IngredientStack> ingredients;
		private final ItemData result;
		private final int craftingCost;
		private final BioForgeTab category;
		private final List<ICondition> conditions;
		private final AdvancementHolder advancement;

		public Result(BioForgingRecipeBuilder builder, AdvancementHolder advancement) {
			id = builder.recipeId;
			category = builder.category;
			result = builder.result;
			ingredients = builder.ingredients;
			craftingCost = builder.craftingCostNutrients;
			conditions = builder.conditions;

			this.advancement = advancement;
		}

		@Override
		public void serializeRecipeData(JsonObject json, DynamicOps<JsonElement> ops) {
			JsonArray jsonArray = new JsonArray();
			for (IngredientStack ingredient : ingredients) {
				jsonArray.add(RecipeJson.ingredientStack(ingredient, ops));
			}
			json.add(RecipeUtil.JsonKeys.INGREDIENTS, jsonArray);

			json.add(RecipeUtil.JsonKeys.RESULT, result.toJson(ops));

			json.addProperty(RecipeUtil.JsonKeys.BIO_FORGE_TAB, String.valueOf(ModBioForgeTabs.REGISTRY.getKey(category)));

			json.addProperty(RecipeUtil.JsonKeys.NUTRIENTS_COST, craftingCost);
		}

		@Override
		public void serializeWikiRecipeData(Consumer<JsonElement> input, Consumer<JsonElement> output, DynamicOps<JsonElement> ops) {
			for (IngredientStack ingredient : ingredients) {
				input.accept(RecipeJson.ingredientStack(ingredient, ops));
			}
			output.accept(result.toJson(ops));
		}

		@Override
		public RecipeSerializer<?> getType() {
			return ModRecipes.BIO_FORGING_SERIALIZER.get();
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		public List<ICondition> getConditions() {
			return conditions;
		}

		@Override
		public AdvancementHolder getAdvancement() {
			return advancement;
		}

	}
}

