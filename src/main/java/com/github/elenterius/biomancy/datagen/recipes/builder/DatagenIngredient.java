package com.github.elenterius.biomancy.datagen.recipes.builder;

import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Objects;
import java.util.stream.Stream;

public record DatagenIngredient(Identifier resourceLocation) implements ICustomIngredient {

	public DatagenIngredient(String itemKey) {
		this(Objects.requireNonNull(Identifier.tryParse(itemKey)));
	}

	public DatagenIngredient(String namespace, String path) {
		this(Objects.requireNonNull(Identifier.tryBuild(namespace, path)));
	}

	public Ingredient ingredient() {
		return toVanilla();
	}

	public JsonElement toJson() {
		return new JsonPrimitive(resourceLocation.toString());
	}

	@Override
	public boolean test(ItemStack stack) {
		return false;
	}

	@Override
	public Stream<Holder<Item>> items() {
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
