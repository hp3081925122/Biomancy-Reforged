package com.github.elenterius.biomancy.datagen.loot;

import com.github.elenterius.biomancy.loot.DespoilLootModifier;
import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class DespoilLootProvider implements LootTableSubProvider {

	protected final HolderLookup.Provider registries;
	private final Map<Identifier, LootTableBuilder> mobToDespoilLootMap = new HashMap<>();

	protected DespoilLootProvider(HolderLookup.Provider registries) {
		this.registries = registries;
	}

	protected boolean canHaveLootTable(EntityType<?> entityType) {
		return entityType == EntityType.PLAYER || entityType == EntityType.VILLAGER || entityType.getCategory() != MobCategory.MISC;
	}

	protected void add(EntityType<?> entityType, LootTable.Builder builder) {
		Identifier entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
		Identifier lootTableId = DespoilLootModifier.getLootTableId(entityType);

		if (canHaveLootTable(entityType)) {
			add(entityTypeId, lootTableId, builder);
		}
		else {
			throw new IllegalStateException(String.format(Locale.ROOT, "Weird despoil loottable '%s' for '%s', not a LivingEntity so should not have despoil loot", lootTableId, entityTypeId));
		}
	}

	protected void add(Identifier entityTypeId, LootTable.Builder builder) {
		Identifier lootTableId = DespoilLootModifier.getLootTableId(entityTypeId);

		mobToDespoilLootMap.put(entityTypeId, new LootTableBuilder(lootTableId, builder));
	}

	private void add(Identifier entityTypeId, Identifier lootTableId, LootTable.Builder builder) {
		mobToDespoilLootMap.put(entityTypeId, new LootTableBuilder(lootTableId, builder));
	}

	public abstract void generate();

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		generate();

		Set<Identifier> uniqueLootTables = Sets.newHashSet();

		for (Map.Entry<Identifier, LootTableBuilder> entry : mobToDespoilLootMap.entrySet()) {
			LootTableBuilder lootTableBuilder = entry.getValue();
			if (lootTableBuilder != null) {
				if (!uniqueLootTables.add(lootTableBuilder.id)) {
					Identifier entityTypeId = entry.getKey();
					throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate despoil loottable '%s' for '%s'", lootTableBuilder.id, entityTypeId));
				}
				else {
					output.accept(ResourceKey.create(Registries.LOOT_TABLE, lootTableBuilder.id), lootTableBuilder.builder);
				}
			}
		}

		mobToDespoilLootMap.clear();
	}

	record LootTableBuilder(Identifier id, LootTable.Builder builder) {}

}
