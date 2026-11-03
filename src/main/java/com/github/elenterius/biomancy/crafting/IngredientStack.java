package com.github.elenterius.biomancy.crafting;

import com.github.elenterius.biomancy.util.ItemStackCounter;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
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

	public static final String ALT_INGREDIENT_KEY = "alt";
	public static final String COUNT_KEY = "count";

	private static final MapCodec<Ingredient> INGREDIENT_MAP_CODEC = Codec.mapEither(Ingredient.CODEC.fieldOf(ALT_INGREDIENT_KEY), Ingredient.MAP_CODEC_NONEMPTY).xmap(
			either -> either.map(i -> i, i -> i),
			ingredient -> !ingredient.isCustom() && ingredient.getValues().length != 1 ? Either.left(ingredient) : Either.right(ingredient)
	);

	public static final Codec<IngredientStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			INGREDIENT_MAP_CODEC.forGetter(IngredientStack::ingredient),
			Codec.INT.optionalFieldOf(COUNT_KEY, 1).forGetter(IngredientStack::count)
	).apply(instance, IngredientStack::new));

	public static final Codec<List<IngredientStack>> LIST_CODEC = CODEC.listOf().xmap(
			list -> list.stream().filter(stack -> !stack.ingredient().isEmpty()).toList(),
			list -> list
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IngredientStack> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, IngredientStack::ingredient,
			ByteBufCodecs.VAR_INT, IngredientStack::count,
			IngredientStack::new
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<IngredientStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

	public ItemStack[] getItems() {
		return ingredient.getItems();
	}

	public boolean testItem(@Nullable ItemStack stack) {
		return ingredient.test(stack);
	}

	public boolean hasSufficientCount(StackedContents itemCounter) {
		IntList stackingIds = ingredient.getStackingIds();

		int n = 0;
		for (int i = 0; i < stackingIds.size(); i++) {
			n += itemCounter.contents.get(stackingIds.getInt(i));
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
		if (count == 1) return List.of(ingredient.getItems());
		return Arrays.stream(ingredient.getItems()).map(this::copyItemStackWithCount).toList();
	}

	private ItemStack copyItemStackWithCount(ItemStack stack) {
		if (count == 0) return ItemStack.EMPTY;
		return stack.copyWithCount(count);
	}

}
