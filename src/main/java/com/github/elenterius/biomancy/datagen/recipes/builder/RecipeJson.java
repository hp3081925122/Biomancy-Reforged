package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class RecipeJson {

	private RecipeJson() {}

	public static JsonElement ingredient(Ingredient ingredient, DynamicOps<JsonElement> ops) {
		if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof DatagenIngredient datagenIngredient) {
			return datagenIngredient.toJson();
		}
		return Ingredient.CODEC.encodeStart(ops, ingredient).getOrThrow();
	}

	public static JsonElement ingredientStack(IngredientStack stack, DynamicOps<JsonElement> ops) {
		if (stack.ingredient().isCustom() && stack.ingredient().getCustomIngredient() instanceof DatagenIngredient datagenIngredient) {
			JsonObject json = datagenIngredient.toJson();
			if (stack.count() > 1) json.addProperty(IngredientStack.COUNT_KEY, stack.count());
			return json;
		}
		return IngredientStack.CODEC.encodeStart(ops, stack).getOrThrow();
	}

	public static JsonElement variableOutput(VariableOutput output, DynamicOps<JsonElement> ops) {
		return VariableOutput.CODEC.encodeStart(ops, output).getOrThrow();
	}

	public static void writeConditions(JsonObject json, List<ICondition> conditions, DynamicOps<JsonElement> ops) {
		if (!conditions.isEmpty()) ICondition.writeConditions(ops, json, conditions);
	}

	public static @Nullable JsonObject advancement(@Nullable AdvancementHolder holder, List<ICondition> conditions, DynamicOps<JsonElement> ops) {
		if (holder == null) return null;
		JsonObject json = Advancement.CODEC.encodeStart(ops, holder.value()).getOrThrow().getAsJsonObject();
		writeConditions(json, conditions, ops);
		return json;
	}

}
