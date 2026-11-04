package com.github.elenterius.biomancy.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public final class ItemHolders {

	@SuppressWarnings("deprecation")
	public static final HolderGetter<Item> LOOKUP = new HolderGetter<>() {
		@Override
		public Optional<Holder.Reference<Item>> get(ResourceKey<Item> id) {
			return BuiltInRegistries.ITEM.get(id);
		}

		@Override
		public Optional<HolderSet.Named<Item>> get(TagKey<Item> id) {
			return Optional.of(BuiltInRegistries.ITEM.get(id).orElseGet(() -> HolderSet.emptyNamed(BuiltInRegistries.ITEM, id)));
		}
	};

	private ItemHolders() {}

	public static HolderSet.Named<Item> tag(TagKey<Item> tag) {
		return LOOKUP.getOrThrow(tag);
	}

	public static Ingredient ingredient(TagKey<Item> tag) {
		return Ingredient.of(tag(tag));
	}

}
