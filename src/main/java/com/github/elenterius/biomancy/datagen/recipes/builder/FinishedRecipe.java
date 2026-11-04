package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FinishedRecipe {

	Identifier getId();

	RecipeSerializer<?> getType();

	List<ICondition> getConditions();

	@Nullable AdvancementHolder getAdvancement();

	void serializeRecipeData(JsonObject json, DynamicOps<JsonElement> ops);

	default JsonObject serializeRecipe(DynamicOps<JsonElement> ops) {
		JsonObject json = new JsonObject();
		json.addProperty("type", String.valueOf(BuiltInRegistries.RECIPE_SERIALIZER.getKey(getType())));
		serializeRecipeData(json, ops);
		RecipeJson.writeConditions(json, getConditions(), ops);
		return json;
	}

}
