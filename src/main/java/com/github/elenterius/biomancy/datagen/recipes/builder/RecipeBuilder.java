package com.github.elenterius.biomancy.datagen.recipes.builder;

import java.util.Optional;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Holder;
import com.github.elenterius.biomancy.util.ItemHolders;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public sealed interface RecipeBuilder<T extends RecipeBuilder<?>> permits BioBrewingRecipeBuilder, BioForgingRecipeBuilder, DecomposingRecipeBuilder, DigestingRecipeBuilder, WorkbenchRecipeBuilder.ShapedBuilder, WorkbenchRecipeBuilder.ShapelessBuilder {

	static String getRecipeFolderName(@Nullable RecipeCategory category, String modId) {
		return category != null ? category.getFolderName() : modId;
	}

	private Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, itemLike).build());
	}

	private Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, tag).build());
	}

	private Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(predicates);
	}

	private String getItemName(ItemLike itemLike) {
		return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
	}

	private String getTagName(TagKey<Item> tag) {
		return tag.location().getPath();
	}

	T unlockedBy(String name, Criterion<?> criterion);

	default T unlockedBy(String name, ItemPredicate predicate) {
		return unlockedBy(name, inventoryTrigger(predicate));
	}

	default T unlockedBy(ItemLike itemLike, Criterion<?> criterion) {
		return unlockedBy("has_" + getItemName(itemLike), criterion);
	}

	default T unlockedBy(ItemLike itemLike) {
		return unlockedBy("has_" + getItemName(itemLike), has(itemLike));
	}

	default T unlockedBy(DatagenIngredient ingredient) {
		Holder<Item> holder = Holder.Reference.createStandAlone(BuiltInRegistries.ITEM, ResourceKey.create(Registries.ITEM, ingredient.resourceLocation()));
		ItemPredicate predicate = new ItemPredicate(Optional.of(HolderSet.direct(holder)), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
		return unlockedBy("has_" + ingredient.resourceLocation().getPath(), inventoryTrigger(predicate));
	}

	default T unlockedBy(TagKey<Item> tag, Criterion<?> criterion) {
		return unlockedBy("has_" + getTagName(tag), criterion);
	}

	default T unlockedBy(TagKey<Item> tag) {
		return unlockedBy("has_" + getTagName(tag), has(tag));
	}

	default void save(Consumer<FinishedRecipe> consumer) {
		save(consumer, null);
	}

	void save(Consumer<FinishedRecipe> consumer, @Nullable RecipeCategory category);

}
