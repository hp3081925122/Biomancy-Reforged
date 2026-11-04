package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import com.github.elenterius.biomancy.util.ItemStackCounter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class BioForgingRecipe implements Recipe<RecipeInput> {

	public static final byte DEFAULT_CRAFTING_COST_NUTRIENTS = 1;
	public static final int MAX_INGREDIENTS = 5;
	private final BioForgeTab tab;
	private final List<IngredientStack> ingredients;
	private final ItemStackTemplate result;
	private ItemStack resultStack;

	private final NonNullList<Ingredient> vanillaIngredients;

	private final int cost;

	public BioForgingRecipe(BioForgeTab tab, ItemStack result, List<IngredientStack> ingredients, int craftingCostNutrients) {
		this(tab, ItemStackTemplate.fromNonEmptyStack(result), ingredients, craftingCostNutrients);
		resultStack = result;
	}

	public BioForgingRecipe(BioForgeTab tab, ItemStackTemplate result, List<IngredientStack> ingredients, int craftingCostNutrients) {
		this.tab = tab;
		this.result = result;
		this.ingredients = ingredients;

		List<Ingredient> flatIngredients = RecipeUtil.flattenIngredientStacks(ingredients);
		vanillaIngredients = NonNullList.createWithCapacity(flatIngredients.size());
		vanillaIngredients.addAll(flatIngredients);

		cost = craftingCostNutrients;
	}

	public int getCraftingCostNutrients() {
		return cost;
	}

	public boolean isCraftable(StackedContents<Holder<Item>> itemCounter) {
		for (IngredientStack ingredientStack : ingredients) {
			if (!ingredientStack.hasSufficientCount(itemCounter)) {
				return false;
			}
		}

		return true;
	}

	public boolean isCraftable(ItemStackCounter itemCounter) {
		int[] residuals = new int[ingredients.size()];
		int totalResidual = 0;
		for (int i = 0; i < ingredients.size(); i++) {
			int count = ingredients.get(i).count();
			residuals[i] = count;
			totalResidual += count;
		}

		for (ItemStackCounter.CountedItem countedItem : itemCounter.getItemCounts()) {
			if (totalResidual <= 0) return true;

			int available = countedItem.amount();

			for (int i = 0; i < ingredients.size(); i++) {
				if (available <= 0) break;

				final int residual = residuals[i];
				if (residual > 0 && ingredients.get(i).testItem(countedItem.stack())) {
					final int amount = Math.min(residual, available);
					residuals[i] -= amount;
					available -= amount;
					totalResidual -= amount;
				}
			}
		}

		return totalResidual <= 0;
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		int[] countedIngredients = new int[ingredients.size()];
		for (int idx = 0; idx < input.size(); idx++) {
			ItemStack stack = input.getItem(idx);
			if (!stack.isEmpty()) {
				for (int i = 0; i < ingredients.size(); i++) {
					if (ingredients.get(i).testItem(stack)) {
						countedIngredients[i] += stack.getCount();
						break;
					}
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
		return width * height >= 0;
	}

	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return resultStack();
	}

	public NonNullList<Ingredient> getIngredients() {
		return vanillaIngredients;
	}

	public List<IngredientStack> getIngredientQuantities() {
		return ingredients;
	}

	public BioForgeTab getTab() {
		return tab;
	}

	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return ModRecipes.BIO_FORGING_SERIALIZER.get();
	}

	@Override
	public RecipeType<? extends Recipe<RecipeInput>> getType() {
		return ModRecipes.BIO_FORGING_RECIPE_TYPE.get();
	}

	public ItemStack getToastSymbol() {
		return new ItemStack(ModItems.BIO_FORGE.get());
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipes.BIO_FORGING_CATEGORY.get();
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(new BioForgingRecipeDisplay(
				ingredients.stream().map(stack -> stack.ingredient().display()).toList(),
				ingredients.stream().map(IngredientStack::count).toList(),
				new SlotDisplay.ItemStackSlotDisplay(result),
				new SlotDisplay.ItemSlotDisplay(ModItems.BIO_FORGE.get())
		));
	}

	public static final class Serializer {

		private static final Codec<List<IngredientStack>> INGREDIENTS_CODEC = IngredientStack.LIST_CODEC.validate(list -> {
			if (list.isEmpty()) return DataResult.error(() -> "No ingredients for recipe");
			if (list.size() > MAX_INGREDIENTS) return DataResult.error(() -> "Too many ingredients for recipe. The maximum is " + MAX_INGREDIENTS);
			return DataResult.success(list);
		});

		public static final MapCodec<BioForgingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				INGREDIENTS_CODEC.fieldOf(RecipeUtil.JsonKeys.INGREDIENTS).forGetter(BioForgingRecipe::getIngredientQuantities),
				ItemStackTemplate.CODEC.fieldOf(RecipeUtil.JsonKeys.RESULT).forGetter(r -> r.result),
				Codec.INT.optionalFieldOf(RecipeUtil.JsonKeys.NUTRIENTS_COST, (int) DEFAULT_CRAFTING_COST_NUTRIENTS).forGetter(BioForgingRecipe::getCraftingCostNutrients),
				BioForgeTab.CODEC.fieldOf(RecipeUtil.JsonKeys.BIO_FORGE_TAB).forGetter(BioForgingRecipe::getTab)
		).apply(instance, (ingredients, result, cost, tab) -> new BioForgingRecipe(tab, result, ingredients, cost)));

		public static final StreamCodec<RegistryFriendlyByteBuf, BioForgingRecipe> STREAM_CODEC = StreamCodec.composite(
				ItemStackTemplate.STREAM_CODEC, r -> r.result,
				IngredientStack.LIST_STREAM_CODEC, BioForgingRecipe::getIngredientQuantities,
				ByteBufCodecs.VAR_INT, BioForgingRecipe::getCraftingCostNutrients,
				BioForgeTab.STREAM_CODEC, BioForgingRecipe::getTab,
				(result, ingredients, cost, tab) -> new BioForgingRecipe(tab, result, ingredients, cost)
		);

		public static final RecipeSerializer<BioForgingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

		public MapCodec<BioForgingRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, BioForgingRecipe> streamCodec() {
			return STREAM_CODEC;
		}

	}

	private ItemStack resultStack() {
		if (resultStack == null) resultStack = result.create();
		return resultStack;
	}

}
