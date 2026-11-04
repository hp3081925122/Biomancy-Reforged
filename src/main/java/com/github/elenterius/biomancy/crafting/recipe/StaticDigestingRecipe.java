package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.mojang.serialization.Codec;
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

public class StaticDigestingRecipe extends StaticProcessingRecipe implements DigestingRecipe {

	public static final int DEFAULT_CRAFTING_TIME_TICKS = 100;
	public static final int DEFAULT_CRAFTING_COST_NUTRIENTS = 1;

	private final Ingredient recipeIngredient;
	private final ItemStackTemplate recipeResult;
	private ItemStack recipeResultStack;

	private final int matchPriority;
	private final NonNullList<Ingredient> vanillaIngredients;

	public StaticDigestingRecipe(ItemStack result, int craftingTimeTicks, int craftingCostNutrients, Ingredient ingredient) {
		this(ItemStackTemplate.fromNonEmptyStack(result), craftingTimeTicks, craftingCostNutrients, ingredient);
		recipeResultStack = result;
	}

	public StaticDigestingRecipe(ItemStackTemplate result, int craftingTimeTicks, int craftingCostNutrients, Ingredient ingredient) {
		super(craftingTimeTicks, craftingCostNutrients);
		recipeIngredient = ingredient;
		recipeResult = result;

		vanillaIngredients = NonNullList.of(recipeIngredient, recipeIngredient);
		matchPriority = RecipeWithMatchPriority.computeMatchPriority(vanillaIngredients);
	}

	@Override
	public int getMatchPriority() {
		return matchPriority;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		return recipeIngredient.test(input.getItem(0));
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		return resultStack().copy();
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height == 1;
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return resultStack();
	}

	@Override
	public Ingredient getIngredient() {
		return recipeIngredient;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return vanillaIngredients;
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return ModRecipes.DIGESTING_SERIALIZER.get();
	}

	@Override
	public RecipeType<? extends Recipe<RecipeInput>> getType() {
		return ModRecipes.DIGESTING_RECIPE_TYPE.get();
	}

	public ItemStack getToastSymbol() {
		return new ItemStack(ModItems.DIGESTER.get());
	}

	public static final class Serializer {

		public static final MapCodec<StaticDigestingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Ingredient.CODEC.fieldOf(RecipeUtil.JsonKeys.INGREDIENT).forGetter(StaticDigestingRecipe::getIngredient),
				ItemStackTemplate.CODEC.fieldOf(RecipeUtil.JsonKeys.RESULT).forGetter(r -> r.recipeResult),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.PROCESSING_TIME, DEFAULT_CRAFTING_TIME_TICKS).forGetter(StaticProcessingRecipe::getCraftingTimeTicks),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.NUTRIENTS_COST, DEFAULT_CRAFTING_COST_NUTRIENTS).forGetter(StaticProcessingRecipe::getCraftingCostNutrients)
		).apply(instance, (ingredient, result, time, cost) -> new StaticDigestingRecipe(result, time, cost, ingredient)));

		public static final StreamCodec<RegistryFriendlyByteBuf, StaticDigestingRecipe> STREAM_CODEC = StreamCodec.composite(
				ItemStackTemplate.STREAM_CODEC, r -> r.recipeResult,
				ByteBufCodecs.VAR_INT, StaticProcessingRecipe::getCraftingTimeTicks,
				ByteBufCodecs.VAR_INT, StaticProcessingRecipe::getCraftingCostNutrients,
				Ingredient.CONTENTS_STREAM_CODEC, StaticDigestingRecipe::getIngredient,
				(result, time, cost, ingredient) -> new StaticDigestingRecipe(result, time, cost, ingredient)
		);

		public static final RecipeSerializer<StaticDigestingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

		public MapCodec<StaticDigestingRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, StaticDigestingRecipe> streamCodec() {
			return STREAM_CODEC;
		}

	}

	private ItemStack resultStack() {
		if (recipeResultStack == null) recipeResultStack = recipeResult.create();
		return recipeResultStack;
	}

}
