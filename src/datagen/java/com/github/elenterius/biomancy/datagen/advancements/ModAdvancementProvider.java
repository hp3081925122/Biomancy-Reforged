package com.github.elenterius.biomancy.datagen.advancements;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import com.github.elenterius.biomancy.util.ItemHolders;
import com.github.elenterius.biomancy.advancements.trigger.SacrificedItemTrigger;
import com.github.elenterius.biomancy.datagen.lang.LangProvider;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.advancements.AdvancementProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModAdvancementProvider extends AdvancementProvider {

	public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, LangProvider lang) {
		super(output, lookupProvider, List.of(new BiomancyAdvancementsGenerator(lang)));
	}

	protected static Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... items) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(items);
	}

	protected static Criterion<InventoryChangeTrigger.TriggerInstance> hasTag(TagKey<Item> tag) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, tag).build());
	}

	protected static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> hasPlacedBlock(Block block) {
		return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block);
	}

	protected static Criterion<KilledTrigger.TriggerInstance> hasKilledEntity(EntityType<?> entityType) {
		return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(BuiltInRegistries.ENTITY_TYPE, entityType));
	}

	protected static Criterion<KilledTrigger.TriggerInstance> hasKilledEntityTag(TagKey<EntityType<?>> tag) {
		return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(BuiltInRegistries.ENTITY_TYPE, tag));
	}

	protected static Criterion<SacrificedItemTrigger.TriggerInstance> hasSacrificedItem(ItemLike item) {
		return SacrificedItemTrigger.TriggerInstance.sacrificedItem(item);
	}

	protected static Criterion<SacrificedItemTrigger.TriggerInstance> hasSacrificedTag(TagKey<Item> tag) {
		return SacrificedItemTrigger.TriggerInstance.sacrificedItem(tag);
	}

	protected static Criterion<TradeTrigger.TriggerInstance> hasTradedItem(ItemLike item) {
		return new Criterion<>(CriteriaTriggers.TRADE, new TradeTrigger.TriggerInstance(
				Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity())),
				Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity())),
				Optional.of(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, item).build())
		));
	}

	protected static Criterion<TradeTrigger.TriggerInstance> hasTradedItems(ItemLike... items) {
		return new Criterion<>(CriteriaTriggers.TRADE, new TradeTrigger.TriggerInstance(
				Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity())),
				Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity())),
				Optional.of(ItemPredicate.Builder.item().of(ItemHolders.LOOKUP, items).build())
		));
	}

	protected static Criterion<RecipeUnlockedTrigger.TriggerInstance> hasUnlockedDefaultRecipe(ItemLike itemLike) {
		return RecipeUnlockedTrigger.unlocked(RecipeBuilder.getDefaultRecipeId(new ItemStackTemplate(itemLike.asItem())));
	}

}
