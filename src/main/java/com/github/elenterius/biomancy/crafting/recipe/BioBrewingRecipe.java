package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.core.HolderLookup;
import java.util.Optional;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class BioBrewingRecipe extends StaticProcessingRecipe {

	public static final short DEFAULT_CRAFTING_TIME_TICKS = 4 * 20;
	public static final short DEFAULT_CRAFTING_COST_NUTRIENTS = 2;
	public static final int JSON_DEFAULT_CRAFTING_TIME_TICKS = 100;
	public static final int MAX_INGREDIENTS = 4;
	public static final int MAX_REACTANT = 1;

	private final List<IngredientStack> ingredients;
	private final Optional<Ingredient> recipeReactant;
	private final ItemStackTemplate result;
	private ItemStack resultStack;

	private final int matchPriority;
	private final NonNullList<Ingredient> vanillaIngredients;

	public BioBrewingRecipe(ItemStack result, int craftingTimeTicks, int craftingCostNutrients, List<IngredientStack> ingredients, Optional<Ingredient> reactant) {
		this(ItemStackTemplate.fromNonEmptyStack(result), craftingTimeTicks, craftingCostNutrients, ingredients, reactant);
		resultStack = result;
	}

	public BioBrewingRecipe(ItemStackTemplate result, int craftingTimeTicks, int craftingCostNutrients, List<IngredientStack> ingredients, Optional<Ingredient> reactant) {
		super(craftingTimeTicks, craftingCostNutrients);
		this.ingredients = ingredients;
		recipeReactant = reactant;
		this.result = result;

		List<Ingredient> flatIngredients = RecipeUtil.flattenIngredientStacks(ingredients);
		recipeReactant.ifPresent(flatIngredients::add);

		vanillaIngredients = NonNullList.createWithCapacity(flatIngredients.size());
		vanillaIngredients.addAll(flatIngredients);

		matchPriority = RecipeWithMatchPriority.computeMatchPriority(vanillaIngredients);
	}

	@Override
	public int getMatchPriority() {
		return matchPriority;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		int lastIndex = input.size() - 1;
		if (!Ingredient.testOptionalIngredient(recipeReactant, input.getItem(lastIndex))) return false;

		int[] countedIngredients = new int[ingredients.size()];
		for (int idx = 0; idx < lastIndex; idx++) {
			ItemStack stack = input.getItem(idx);
			if (stack.isEmpty()) continue;

			for (int i = 0; i < ingredients.size(); i++) {
				IngredientStack requiredIngredient = ingredients.get(i);
				if (requiredIngredient.testItem(stack) && countedIngredients[i] < requiredIngredient.count()) {
					countedIngredients[i] += stack.getCount();
					break;
				}
			}
		}

		for (int i = 0; i < ingredients.size(); i++) {
			if (countedIngredients[i] < ingredients.get(i).count()) return false;
		}

		return true;
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		return resultStack().copy();
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= ingredients.size();
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return resultStack();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return vanillaIngredients;
	}

	public List<IngredientStack> getIngredientQuantities() {
		return ingredients;
	}

	public Optional<Ingredient> getReactant() {
		return recipeReactant;
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return ModRecipes.BIO_BREWING_SERIALIZER.get();
	}

	@Override
	public RecipeType<? extends Recipe<RecipeInput>> getType() {
		return ModRecipes.BIO_BREWING_RECIPE_TYPE.get();
	}

	public ItemStack getToastSymbol() {
		return new ItemStack(ModItems.BIO_LAB.get());
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipes.BIO_BREWING_CATEGORY.get();
	}

	public static final class Serializer {

		private static final Codec<List<IngredientStack>> INGREDIENTS_CODEC = IngredientStack.LIST_CODEC.validate(list -> {
			if (list.isEmpty()) return DataResult.error(() -> "No ingredients found for bio brewing recipe");
			if (list.size() > MAX_INGREDIENTS) return DataResult.error(() -> "Too many ingredients for bio brewing recipe. Max amount is %d".formatted(MAX_INGREDIENTS));
			for (IngredientStack ingredientStack : list) {
				int count = ingredientStack.count();
				if (count > 64) return DataResult.error(() -> "Ingredient quantity of %d is larger than 64".formatted(count));
			}
			return DataResult.success(list);
		});

		public static final MapCodec<BioBrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				INGREDIENTS_CODEC.fieldOf(RecipeUtil.JsonKeys.INGREDIENTS).forGetter(BioBrewingRecipe::getIngredientQuantities),
				Ingredient.CODEC.optionalFieldOf(RecipeUtil.JsonKeys.REACTANT).forGetter(BioBrewingRecipe::getReactant),
				ItemStackTemplate.CODEC.fieldOf(RecipeUtil.JsonKeys.RESULT).forGetter(r -> r.result),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.PROCESSING_TIME, JSON_DEFAULT_CRAFTING_TIME_TICKS).forGetter(StaticProcessingRecipe::getCraftingTimeTicks),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.NUTRIENTS_COST, (int) DEFAULT_CRAFTING_COST_NUTRIENTS).forGetter(StaticProcessingRecipe::getCraftingCostNutrients)
		).apply(instance, (ingredients, reactant, result, time, cost) -> new BioBrewingRecipe(result, time, cost, ingredients, reactant)));

		public static final StreamCodec<RegistryFriendlyByteBuf, BioBrewingRecipe> STREAM_CODEC = StreamCodec.composite(
				ItemStackTemplate.STREAM_CODEC, r -> r.result,
				Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, BioBrewingRecipe::getReactant,
				ByteBufCodecs.VAR_INT, StaticProcessingRecipe::getCraftingTimeTicks,
				ByteBufCodecs.VAR_INT, StaticProcessingRecipe::getCraftingCostNutrients,
				IngredientStack.LIST_STREAM_CODEC, BioBrewingRecipe::getIngredientQuantities,
				(result, reactant, time, cost, ingredients) -> new BioBrewingRecipe(result, time, cost, ingredients, reactant)
		);

		public static final RecipeSerializer<BioBrewingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

		public MapCodec<BioBrewingRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, BioBrewingRecipe> streamCodec() {
			return STREAM_CODEC;
		}

	}

	private ItemStack resultStack() {
		if (resultStack == null) resultStack = result.create();
		return resultStack;
	}

}
