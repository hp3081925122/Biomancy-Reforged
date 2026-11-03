package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Objects;
import java.util.stream.Stream;

public record DatagenIngredient(ResourceLocation resourceLocation) implements ICustomIngredient {

	public DatagenIngredient(String itemKey) {
		this(Objects.requireNonNull(ResourceLocation.tryParse(itemKey)));
	}

	public DatagenIngredient(String namespace, String path) {
		this(Objects.requireNonNull(ResourceLocation.tryBuild(namespace, path)));
	}

	public Ingredient ingredient() {
		return toVanilla();
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("item", resourceLocation.toString());
		return json;
	}

	@Override
	public boolean test(ItemStack stack) {
		return false;
	}

	@Override
	public Stream<ItemStack> getItems() {
		return Stream.empty();
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public IngredientType<?> getType() {
		throw new UnsupportedOperationException("DatagenIngredient is only serialized by the Biomancy recipe builders");
	}

}
