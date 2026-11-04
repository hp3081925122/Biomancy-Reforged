package com.github.elenterius.biomancy.crafting;

import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.util.ItemStackCounter;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public record IngredientStack(Ingredient ingredient, int count) {

	public static final String INGREDIENT_KEY = "ingredient";
	public static final String ALT_INGREDIENT_KEY = "alt";
	public static final String COUNT_KEY = "count";

	private static final MapCodec<Ingredient> INGREDIENT_MAP_CODEC = Codec.mapEither(Ingredient.CODEC.fieldOf(INGREDIENT_KEY), Ingredient.CODEC.fieldOf(ALT_INGREDIENT_KEY)).xmap(
			either -> either.map(i -> i, i -> i),
			Either::left
	);

	public static final Codec<IngredientStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			INGREDIENT_MAP_CODEC.forGetter(IngredientStack::ingredient),
			Codec.INT.optionalFieldOf(COUNT_KEY, 1).forGetter(IngredientStack::count)
	).apply(instance, IngredientStack::new));

	public static final Codec<List<IngredientStack>> LIST_CODEC = CODEC.listOf().xmap(
			list -> list.stream().filter(stack -> !isExplicitlyEmpty(stack.ingredient())).toList(),
			list -> list
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IngredientStack> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, IngredientStack::ingredient,
			ByteBufCodecs.VAR_INT, IngredientStack::count,
			IngredientStack::new
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<IngredientStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

	private static boolean isExplicitlyEmpty(Ingredient ingredient) {
		if (ingredient.isCustom()) return false;
		return ingredient.getValues().unwrap().right().map(List::isEmpty).orElse(false);
	}

	public ItemStack[] getItems() {
		return getItems(ingredient);
	}

	public static ItemStack[] getItems(Ingredient ingredient) {
		if (ingredient.getCustomIngredient() instanceof EssenceIngredient essenceIngredient) {
			return new ItemStack[]{essenceIngredient.getDisplayStack()};
		}
		if (ingredient.getCustomIngredient() instanceof DataComponentIngredient componentIngredient) {
			return componentIngredient.items().map(holder -> new ItemStack(holder, 1, componentIngredient.components())).toArray(ItemStack[]::new);
		}
		return ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new);
	}

	public boolean testItem(@Nullable ItemStack stack) {
		return ingredient.test(stack);
	}

	public boolean hasSufficientCount(StackedContents<Holder<Item>> itemCounter) {
		int n = 0;
		for (Holder<Item> holder : ingredient.items().toList()) {
			n += itemCounter.amounts.getInt(holder);
			if (n >= count) return true;
		}

		return false;
	}

	public boolean hasSufficientCount(ItemStackCounter itemCounter) {
		List<ItemStackCounter.CountedItem> itemCounts = itemCounter.getItemCounts();

		int n = 0;
		for (ItemStackCounter.CountedItem countedItem : itemCounts) {
			if (ingredient.test(countedItem.stack())) n += countedItem.amount();
			if (n >= count) return true;
		}

		return false;
	}

	public List<ItemStack> getItemsWithCount() {
		if (count == 1) return List.of(getItems());
		return Arrays.stream(getItems()).map(this::copyItemStackWithCount).toList();
	}

	private ItemStack copyItemStackWithCount(ItemStack stack) {
		if (count == 0) return ItemStack.EMPTY;
		return stack.copyWithCount(count);
	}

}
