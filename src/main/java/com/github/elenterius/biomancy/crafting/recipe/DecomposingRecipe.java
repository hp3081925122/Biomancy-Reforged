package com.github.elenterius.biomancy.crafting.recipe;

import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class DecomposingRecipe extends StaticProcessingRecipe {
	public static final short DEFAULT_CRAFTING_COST_NUTRIENTS = 1;
	public static final int DEFAULT_CRAFTING_TIME_TICKS = 100;
	public static final int MAX_INGREDIENTS = 1;
	public static final int MAX_OUTPUTS = 6;

	private final IngredientStack ingredientStack;
	private final List<VariableOutput> outputs;

	private final int matchPriority;
	private final NonNullList<Ingredient> vanillaIngredients;

	public DecomposingRecipe(List<VariableOutput> outputs, IngredientStack ingredientStack, int craftingTimeTicks, int craftingCostNutrients) {
		super(craftingTimeTicks, craftingCostNutrients);
		this.ingredientStack = ingredientStack;
		this.outputs = outputs;

		List<Ingredient> flatIngredients = RecipeUtil.flattenIngredientStacks(List.of(ingredientStack));
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
		ItemStack stack = input.getItem(0);
		return ingredientStack.ingredient().test(stack) && stack.getCount() >= ingredientStack.count();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return outputs.get(0).getItemStack();
	}

	@Override
	public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
		return outputs.get(0).getItemStack().copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height == 1;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return vanillaIngredients;
	}

	public IngredientStack getIngredientQuantity() {
		return ingredientStack;
	}

	public List<VariableOutput> getOutputs() {
		return outputs;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.DECOMPOSING_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipes.DECOMPOSING_RECIPE_TYPE.get();
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(ModItems.DECOMPOSER.get());
	}

	public static class Serializer implements RecipeSerializer<DecomposingRecipe> {

		private static final Codec<List<VariableOutput>> OUTPUTS_CODEC = VariableOutput.CODEC.listOf().validate(list -> list.size() > MAX_OUTPUTS
				? DataResult.error(() -> "Too many outputs for decomposing recipe. Max amount is %d".formatted(MAX_OUTPUTS))
				: DataResult.success(list));

		public static final MapCodec<DecomposingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				IngredientStack.CODEC.fieldOf(RecipeUtil.JsonKeys.INGREDIENT).forGetter(DecomposingRecipe::getIngredientQuantity),
				OUTPUTS_CODEC.fieldOf(RecipeUtil.JsonKeys.RESULTS).forGetter(DecomposingRecipe::getOutputs),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.PROCESSING_TIME, DEFAULT_CRAFTING_TIME_TICKS).forGetter(StaticProcessingRecipe::getCraftingTimeTicks),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.NUTRIENTS_COST, (int) DEFAULT_CRAFTING_COST_NUTRIENTS).forGetter(StaticProcessingRecipe::getCraftingCostNutrients)
		).apply(instance, (ingredient, outputs, time, cost) -> new DecomposingRecipe(outputs, ingredient, time, cost)));

		public static final StreamCodec<RegistryFriendlyByteBuf, DecomposingRecipe> STREAM_CODEC = StreamCodec.composite(
				IngredientStack.STREAM_CODEC, DecomposingRecipe::getIngredientQuantity,
				ByteBufCodecs.VAR_INT, StaticProcessingRecipe::getCraftingTimeTicks,
				ByteBufCodecs.VAR_INT, StaticProcessingRecipe::getCraftingCostNutrients,
				VariableOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), DecomposingRecipe::getOutputs,
				(ingredient, time, cost, outputs) -> new DecomposingRecipe(outputs, ingredient, time, cost)
		);

		@Override
		public MapCodec<DecomposingRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, DecomposingRecipe> streamCodec() {
			return STREAM_CODEC;
		}

	}

}
