package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.loot.CatMorningGiftLootModifier;
import com.github.elenterius.biomancy.loot.CopyBlockEntityDataFunction;
import com.github.elenterius.biomancy.loot.DespoilLootModifier;
import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collections;
import java.util.Set;

import static com.github.elenterius.biomancy.BiomancyMod.rl;

public final class ModLoot {

	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BiomancyMod.MOD_ID);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DespoilLootModifier>> DESPOIL_SERIALIZER = GLOBAL_MODIFIERS.register("despoil", () -> DespoilLootModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<CatMorningGiftLootModifier>> CAT_MORNING_GIFT_SERIALIZER = GLOBAL_MODIFIERS.register("cat_morning_gift", () -> CatMorningGiftLootModifier.CODEC);

	public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, BiomancyMod.MOD_ID);
	public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CopyBlockEntityDataFunction>> COPY_BLOCK_ENTITY_DATA = LOOT_FUNCTIONS.register("copy_block_entity_data", () -> new LootItemFunctionType<>(CopyBlockEntityDataFunction.CODEC));

	private ModLoot() {}

	public static final class Entity {
		private static final Set<ResourceKey<LootTable>> LOOT_TABLE_KEYS = Sets.newHashSet();

		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_2 = register("entities/flesh_blob/size_2");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_3 = register("entities/flesh_blob/size_3");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_4 = register("entities/flesh_blob/size_4");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_5 = register("entities/flesh_blob/size_5");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_6 = register("entities/flesh_blob/size_6");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_7 = register("entities/flesh_blob/size_7");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_8 = register("entities/flesh_blob/size_8");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_9 = register("entities/flesh_blob/size_9");
		public static final ResourceKey<LootTable> FLESH_BLOB_SIZE_10 = register("entities/flesh_blob/size_10");

		public static final ResourceKey<LootTable> FLESH_SHEEP_UNSHORN = register("entities/flesh_sheep/unshorn");

		private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOOT_TABLE_KEYS = Collections.unmodifiableSet(LOOT_TABLE_KEYS);

		private Entity() {}

		private static ResourceKey<LootTable> register(String id) {
			return register(ResourceKey.create(Registries.LOOT_TABLE, rl(id)));
		}

		private static ResourceKey<LootTable> register(ResourceKey<LootTable> key) {
			if (LOOT_TABLE_KEYS.add(key)) {
				return key;
			}
			else {
				throw new IllegalArgumentException(key + " is already a registered built-in loot table");
			}
		}

		public static Set<ResourceKey<LootTable>> all() {
			return IMMUTABLE_LOOT_TABLE_KEYS;
		}

	}

}
