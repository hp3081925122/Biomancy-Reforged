package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.component.DataComponents;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.AnyFoodIngredient;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FoodDigestingRecipe extends DynamicProcessingRecipe implements DigestingRecipe {

	private final int multiplier;
	private final ItemStackTemplate resultBaseItem;
	private ItemStack resultBaseItemStack;
	private final Ingredient ingredient;

	private final int matchPriority;
	private final NonNullList<Ingredient> vanillaIngredients;

	public FoodDigestingRecipe(int multiplier, ItemStack resultBaseItem) {
		this(multiplier, ItemStackTemplate.fromNonEmptyStack(resultBaseItem));
		resultBaseItemStack = resultBaseItem;
	}

	public FoodDigestingRecipe(int multiplier, ItemStackTemplate resultBaseItem) {
		super(ModRecipes.DIGESTING_RECIPE_TYPE.get());
		this.multiplier = multiplier;
		this.resultBaseItem = resultBaseItem;

		ingredient = AnyFoodIngredient.ingredient();

		vanillaIngredients = NonNullList.of(ingredient, ingredient);
		matchPriority = RecipeWithMatchPriority.computeMatchPriority(vanillaIngredients);
	}

	public static int getFoodNutrition(ItemStack stack) {
		if (stack.isEmpty()) return 0;

		FoodProperties foodProperties = stack.get(DataComponents.FOOD);
		return foodProperties != null ? foodProperties.nutrition() : 0;
	}

	public int getMultiplier() {
		return multiplier;
	}

	public ItemStack getResultBaseItem() {
		return resultStack();
	}

	@Override
	public int getMatchPriority() {
		return matchPriority;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		int nutrition = getFoodNutrition(input.getItem(0));
		return nutrition > 0;
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		int nutrition = getFoodNutrition(input.getItem(0));
		if (nutrition <= 0) return ItemStack.EMPTY;

		int count = Mth.clamp(nutrition * multiplier, 1, 64 * 2);
		return resultStack().copyWithCount(count);
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height == 1;
	}

	@Override
	public Ingredient getIngredient() {
		return ingredient;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return vanillaIngredients;
	}

	@Override
	public int getCraftingTimeTicks(RecipeInput input) {
		int nutrition = getFoodNutrition(input.getItem(0));
		return nutrition > 0 ? Mth.ceil(200 + 190 * Math.log(nutrition)) : 0;
	}

	@Override
	public int getCraftingCostNutrients(RecipeInput input) {
		float sixtySecondsInTicks = 1200;
		return 1 + Mth.floor(getCraftingTimeTicks(input) / sixtySecondsInTicks);
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return ModRecipes.FOOD_DIGESTING_SERIALIZER.get();
	}

	public ItemStack getToastSymbol() {
		return new ItemStack(ModItems.DIGESTER.get());
	}

	public static final class Serializer {

		public static final MapCodec<FoodDigestingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.optionalFieldOf("multiplier", 1).forGetter(FoodDigestingRecipe::getMultiplier),
				ItemStackTemplate.CODEC.fieldOf("result_base").forGetter(r -> r.resultBaseItem)
		).apply(instance, (multiplier, result) -> new FoodDigestingRecipe(multiplier, result)));

		public static final StreamCodec<RegistryFriendlyByteBuf, FoodDigestingRecipe> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, FoodDigestingRecipe::getMultiplier,
				ItemStackTemplate.STREAM_CODEC, r -> r.resultBaseItem,
				(multiplier, result) -> new FoodDigestingRecipe(multiplier, result)
		);

		public static final RecipeSerializer<FoodDigestingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

		public MapCodec<FoodDigestingRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, FoodDigestingRecipe> streamCodec() {
			return STREAM_CODEC;
		}

	}

	public static class RecipeBuilder {

		public static void save(RecipeOutput output, int multiplier, Item item) {
			save(output, multiplier, new ItemStack(item));
		}

		public static void save(RecipeOutput output, int multiplier, ItemStack stack) {
			Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
			String subFolder = ModRecipes.DIGESTING_RECIPE_TYPE.getId().getPath();
			save(output, multiplier, stack, BiomancyMod.rl(subFolder + "/" + key.getPath() + "_from_digesting_dynamic_food"));
		}

		public static void save(RecipeOutput output, int multiplier, ItemStack stack, Identifier id) {
			output.accept(ResourceKey.create(Registries.RECIPE, id), new FoodDigestingRecipe(multiplier, stack.copyWithCount(1)), null);
		}

	}

	private ItemStack resultStack() {
		if (resultBaseItemStack == null) resultBaseItemStack = resultBaseItem.create();
		return resultBaseItemStack;
	}

}
