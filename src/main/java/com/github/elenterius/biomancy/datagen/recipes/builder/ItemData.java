package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemData {

	private final ResourceLocation registryName;
	private final int count;
	private final DataComponentPatch components;

	public ItemData(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemData(ItemLike item) {
		this(item, 1);
	}

	public ItemData(ItemStack stack, int count) {
		this(stack.getItem(), stack.getComponentsPatch(), count);
	}

	public ItemData(ItemLike item, int count) {
		this(item, DataComponentPatch.EMPTY, count);
	}

	public ItemData(ItemLike item, DataComponentPatch components, int count) {
		this.registryName = BuiltInRegistries.ITEM.getKey(item.asItem());
		this.components = components;
		this.count = count;
	}

	public static ItemData from(String namespace, String path) {
		return new ItemData(ResourceLocation.fromNamespaceAndPath(namespace, path));
	}

	public ItemData(ResourceLocation registryName) {
		this(registryName, DataComponentPatch.EMPTY, 1);
	}

	public ItemData(ResourceLocation registryName, DataComponentPatch components, int count) {
		this.registryName = registryName;
		this.components = components;
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public ResourceLocation getRegistryName() {
		return registryName;
	}

	public JsonObject toJson(DynamicOps<JsonElement> ops) {
		JsonObject json = new JsonObject();
		json.addProperty("id", registryName.toString());
		if (count > 1) json.addProperty("count", count);
		if (!components.isEmpty()) json.add("components", DataComponentPatch.CODEC.encodeStart(ops, components).getOrThrow());
		return json;
	}

	public String getItemPath() {
		return registryName.getPath();
	}

}
