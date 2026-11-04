package com.github.elenterius.biomancy.datagen.recipes;

import com.github.elenterius.biomancy.datagen.recipes.builder.DatagenIngredient;
import com.github.elenterius.biomancy.crafting.recipe.FoodDigestingRecipe;
import com.github.elenterius.biomancy.datagen.recipes.builder.DigestingRecipeBuilder;
import com.github.elenterius.biomancy.datagen.recipes.builder.FinishedRecipeOutput;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.datagen.recipes.builder.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Consumer;

public class DigestingRecipeProvider {

	protected static String getItemName(ItemLike itemLike) {
		Identifier key = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
		return key != null ? key.getPath() : "unknown";
	}

	protected static String getTagName(TagKey<Item> tag) {
		return tag.location().getPath();
	}

	protected DigestingRecipeBuilder nutrientPasteRecipe(int count, TagKey<Item> ingredient) {
		return simpleRecipe(ModItems.NUTRIENT_PASTE.get(), count, ingredient);
	}

	protected DigestingRecipeBuilder nutrientPasteRecipe(int count, ItemLike ingredient) {
		return simpleRecipe(ModItems.NUTRIENT_PASTE.get(), count, ingredient);
	}

	protected DigestingRecipeBuilder simpleRecipe(ItemLike result, int count, TagKey<Item> ingredient) {
		return DigestingRecipeBuilder.create(result, count, getTagName(ingredient)).setIngredient(ingredient).unlockedBy(ingredient);
	}

	protected DigestingRecipeBuilder simpleRecipe(ItemLike result, int count, ItemLike ingredient) {
		return DigestingRecipeBuilder.create(result, count, getItemName(ingredient)).setIngredient(ingredient).unlockedBy(ingredient);
	}

	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		buildFromFoodRecipes(consumer);
		buildFromOrganicRecipes(consumer);

		buildFarmersDelightRecipes(consumer);
	}

	private void buildFromFoodRecipes(Consumer<FinishedRecipe> consumer) {
		FoodDigestingRecipe.RecipeBuilder.save(FinishedRecipeOutput.of(consumer), 1, ModItems.NUTRIENT_PASTE.get());
	}

	private void buildFromOrganicRecipes(Consumer<FinishedRecipe> consumer) {
		nutrientPasteRecipe(1, Items.SHORT_GRASS).save(consumer);
		nutrientPasteRecipe(1, Items.SEAGRASS).save(consumer);
		nutrientPasteRecipe(1, Items.VINE).save(consumer);
		nutrientPasteRecipe(1, Items.FERN).save(consumer);
		nutrientPasteRecipe(1, Items.HANGING_ROOTS).save(consumer);

		nutrientPasteRecipe(1, Items.BAMBOO).addCraftingTimeModifier(60).save(consumer);

		int sugarNutrition = 1;
		nutrientPasteRecipe(sugarNutrition, Items.SUGAR_CANE).addCraftingTimeModifier(20).save(consumer);

		int seedNutrition = 1;
		nutrientPasteRecipe(seedNutrition, Tags.Items.SEEDS).addCraftingTimeModifier(40).save(consumer);

		nutrientPasteRecipe(1, ItemTags.SMALL_FLOWERS).addCraftingTimeModifier(-20).save(consumer);
		nutrientPasteRecipe(1, ItemTags.LEAVES).addCraftingTimeModifier(25).save(consumer);
		nutrientPasteRecipe(1, ItemTags.SAPLINGS).addCraftingTimeModifier(15).save(consumer);

		nutrientPasteRecipe(1, Items.MOSS_CARPET).addCraftingTimeModifier(-20).save(consumer);
		nutrientPasteRecipe(2, Items.MOSS_BLOCK).setCraftingCost(3).addCraftingTimeModifier(20).save(consumer);

		nutrientPasteRecipe(2, Items.NETHER_WART).setCraftingCost(2).addCraftingTimeModifier(-40).save(consumer);
		nutrientPasteRecipe(2, Items.CACTUS).setCraftingCost(3).save(consumer);
		nutrientPasteRecipe(2, Items.LARGE_FERN).save(consumer);
		nutrientPasteRecipe(2, Items.TALL_GRASS).save(consumer);
		DigestingRecipeBuilder.create(ModItems.NUTRIENT_PASTE.get(), 2, "tall_flowers").setIngredient(Tags.Items.FLOWERS_TALL).unlockedBy(Tags.Items.FLOWERS_TALL).save(consumer);
		nutrientPasteRecipe(2, Items.NETHER_SPROUTS).save(consumer);
		nutrientPasteRecipe(2, Items.WEEPING_VINES).save(consumer);
		nutrientPasteRecipe(2, Items.TWISTING_VINES).save(consumer);
		nutrientPasteRecipe(2, Items.WARPED_ROOTS).save(consumer);
		nutrientPasteRecipe(2, Items.CRIMSON_ROOTS).save(consumer);
		nutrientPasteRecipe(2, Items.LILY_PAD).save(consumer);

		nutrientPasteRecipe(2, Items.SMALL_DRIPLEAF).save(consumer);
		nutrientPasteRecipe(4, Items.BIG_DRIPLEAF).setCraftingCost(3).save(consumer);

		nutrientPasteRecipe(3, Items.HONEYCOMB).save(consumer);
		nutrientPasteRecipe(3, Items.SEA_PICKLE).save(consumer);
		nutrientPasteRecipe(3, Items.WARPED_WART_BLOCK).setCraftingCost(3).save(consumer);
		nutrientPasteRecipe(9 * 2, Items.NETHER_WART_BLOCK).setCraftingCost(9 * 2 - 2).save(consumer);
		nutrientPasteRecipe(4, Items.SHROOMLIGHT).setCraftingCost(3).save(consumer);

		nutrientPasteRecipe(Foods.DRIED_KELP.nutrition(), Items.KELP).setCraftingCost(3).addCraftingTimeModifier(35).save(consumer);
		nutrientPasteRecipe(9 * Foods.DRIED_KELP.nutrition(), Items.DRIED_KELP_BLOCK).addCraftingTimeModifier(-Math.round(20 * 4.5f)).save(consumer);

		int wheatNutrition = Math.max(1, Foods.BREAD.nutrition() / 3);
		nutrientPasteRecipe(wheatNutrition, Items.WHEAT).save(consumer);
		nutrientPasteRecipe(9 * wheatNutrition, Items.HAY_BLOCK).addCraftingTimeModifier(40).save(consumer);

		nutrientPasteRecipe(4, Items.COCOA_BEANS).setCraftingCost(3).addCraftingTimeModifier(60).save(consumer);

		int eggNutrition = 1;
		nutrientPasteRecipe(eggNutrition, Items.EGG).addCraftingTimeModifier(20).save(consumer);
		nutrientPasteRecipe(eggNutrition, ModItems.ACIDIC_EGG.get()).addCraftingTimeModifier(20).save(consumer);
		nutrientPasteRecipe(2, Items.TURTLE_EGG).save(consumer);
		nutrientPasteRecipe(6, Items.SNIFFER_EGG).setCraftingCost(3).save(consumer);

		int mushroomNutrition = Math.max(1, Foods.MUSHROOM_STEW.nutrition() / 2);
		nutrientPasteRecipe(mushroomNutrition, Tags.Items.MUSHROOMS).save(consumer);
		nutrientPasteRecipe(mushroomNutrition, Items.WARPED_FUNGUS).save(consumer);
		nutrientPasteRecipe(mushroomNutrition, Items.CRIMSON_FUNGUS).save(consumer);
		nutrientPasteRecipe(mushroomNutrition * 2, Items.RED_MUSHROOM_BLOCK).setCraftingCost(3).save(consumer);
		nutrientPasteRecipe(mushroomNutrition * 2, Items.BROWN_MUSHROOM_BLOCK).setCraftingCost(3).save(consumer);
		nutrientPasteRecipe(2, Items.MUSHROOM_STEM).setCraftingCost(3).save(consumer);

		int milkNutrition = 2;

		int cakeNutrition = 3 * wheatNutrition + 2 * sugarNutrition + eggNutrition + 3 * milkNutrition;
		nutrientPasteRecipe(cakeNutrition, Items.CAKE).save(consumer);

		nutrientPasteRecipe(7 * Foods.MELON_SLICE.nutrition(), Items.MELON).setCraftingCost(3).save(consumer);

		int pumpkinNutrition = Foods.PUMPKIN_PIE.nutrition() - sugarNutrition - eggNutrition;
		nutrientPasteRecipe(pumpkinNutrition, Items.PUMPKIN).setCraftingCost(3).save(consumer);
		nutrientPasteRecipe(pumpkinNutrition - 4 * seedNutrition, Items.CARVED_PUMPKIN).addCraftingTimeModifier(-100).save(consumer);
	}

	private static final String FARMERS_DELIGHT = "farmersdelight";

	private DigestingRecipeBuilder farmersDelightRecipe(int count, String itemPath) {
		DatagenIngredient ingredient = new DatagenIngredient(FARMERS_DELIGHT, itemPath);
		return DigestingRecipeBuilder.create(ModItems.NUTRIENT_PASTE.get(), count, itemPath).setIngredient(ingredient.ingredient()).unlockedBy(ingredient).ifModLoaded(FARMERS_DELIGHT);
	}

	private void buildFarmersDelightRecipes(Consumer<FinishedRecipe> consumer) {
		farmersDelightRecipe(1, "rotten_tomato").addCraftingTimeModifier(50).save(consumer);

		int mushroomNutrition = Math.max(1, Foods.MUSHROOM_STEW.nutrition() / 2);
		farmersDelightRecipe(mushroomNutrition * 5, "brown_mushroom_colony").save(consumer);
		farmersDelightRecipe(mushroomNutrition * 5, "red_mushroom_colony").save(consumer);

		farmersDelightRecipe(Foods.MELON_SLICE.nutrition() * 4, "melon_juice").setCraftingCost(3).save(consumer);
		farmersDelightRecipe(Foods.APPLE.nutrition() * 2 + 1, "apple_cider").setCraftingCost(3).save(consumer);
		farmersDelightRecipe(4 * 2 + 2, "hot_cocoa").setCraftingCost(3).addCraftingTimeModifier(30).save(consumer);
	}

}