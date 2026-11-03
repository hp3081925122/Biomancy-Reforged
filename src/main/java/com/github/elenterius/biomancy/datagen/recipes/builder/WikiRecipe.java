package com.github.elenterius.biomancy.datagen.recipes.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public interface WikiRecipe extends FinishedRecipe {

	String ESSENCE_INGREDIENT_TYPE = "biomancy:essence";

	default JsonObject serializeWikiRecipe(DynamicOps<JsonElement> ops) {
		JsonObject root = new JsonObject();
		root.addProperty("type", String.valueOf(BuiltInRegistries.RECIPE_SERIALIZER.getKey(getType())));

		JsonObject input = new JsonObject();
		AtomicInteger i = new AtomicInteger();

		JsonObject output = new JsonObject();
		AtomicInteger o = new AtomicInteger();

		serializeWikiRecipeData(
				in -> input.add(Integer.toString(i.getAndIncrement()), transform(in)),
				out -> output.add(Integer.toString(o.getAndIncrement()), transform(out)),
				ops
		);

		root.add("input", input);
		root.add("output", output);
		return root;
	}

	static JsonElement transform(JsonElement json) {
		if (json instanceof JsonObject jsonObject) {
			if (jsonObject.has("item")) {
				if (jsonObject.has("id")) throw new RuntimeException("JSON already contains 'id' member!");

				JsonElement item = jsonObject.get("item");
				jsonObject.remove("item");

				if (item instanceof JsonObject) throw new RuntimeException("Unknown item type: " + item);
				jsonObject.add("id", item);
			}
			else if (jsonObject.has("tag")) {
				if (jsonObject.has("id")) throw new RuntimeException("JSON already contains 'id' member!");

				String tag = GsonHelper.getAsString(jsonObject, "tag");
				jsonObject.remove("tag");
				jsonObject.addProperty("id", "#" + tag);
			}
			else if (jsonObject.has("type")) {
				String type = GsonHelper.getAsString(jsonObject, "type");
				if (!type.equals(ESSENCE_INGREDIENT_TYPE)) throw new RuntimeException("Unhandled ingredient type: " + type);

				JsonObject nbt = new JsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.deepCopy().entrySet()) {
					if (entry.getKey().equals("type") || entry.getKey().equals("count")) continue;
					nbt.add(entry.getKey(), entry.getValue());
					jsonObject.remove(entry.getKey());
				}
				jsonObject.remove("type");
				jsonObject.addProperty("id", type);
				jsonObject.add("nbt", nbt);
			}
		}
		return json;
	}

	void serializeWikiRecipeData(Consumer<JsonElement> input, Consumer<JsonElement> output, DynamicOps<JsonElement> ops);

}
