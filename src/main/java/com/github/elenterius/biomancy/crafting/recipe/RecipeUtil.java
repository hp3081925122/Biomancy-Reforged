package com.github.elenterius.biomancy.crafting.recipe;

import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RecipeUtil {

	public static final class JsonKeys {
		public static final String INGREDIENT = "ingredient";
		public static final String INGREDIENTS = "ingredients";
		public static final String REACTANT = "reactant";
		public static final String RESULT = "result";
		public static final String RESULTS = "results";
		public static final String PROCESSING_TIME = "processingTime";
		public static final String NUTRIENTS_COST = "nutrientsCost";
		public static final String BIO_FORGE_TAB = BioForgeTab.JSON_KEY;

		public static final String GROUP = "group";
		public static final String CONDITIONS = "conditions";

		public static final String COUNT = "count";
		public static final String TAG = "tag";
		public static final String ID = "id";

		private JsonKeys() {}
	}

	private RecipeUtil() {}

	public static JsonObject writeItemStack(DynamicOps<JsonElement> ops, ItemStack stack) {
		return ItemStack.CODEC.encodeStart(ops, stack).getOrThrow().getAsJsonObject();
	}

	public static ItemStack readItemStack(DynamicOps<JsonElement> ops, JsonObject json) {
		return ItemStack.CODEC.parse(ops, json).getOrThrow();
	}

	@SuppressWarnings("deprecation")
	public static void writeItem(FriendlyByteBuf buffer, @Nullable Item item) {
		if (item == null || item == Items.AIR) {
			buffer.writeBoolean(false);
		}
		else {
			buffer.writeBoolean(true);
			buffer.writeId(BuiltInRegistries.ITEM, item);
		}
	}

	@SuppressWarnings("deprecation")
	public static @Nullable Item readItem(FriendlyByteBuf buffer) {
		return !buffer.readBoolean() ? null : buffer.readById(BuiltInRegistries.ITEM);
	}

	public static Ingredient readIngredient(JsonObject json, String memberName) {
		return Ingredient.fromJson((GsonHelper.isArrayNode(json, memberName) ? GsonHelper.getAsJsonArray(json, memberName) : GsonHelper.getAsJsonObject(json, memberName)));
	}

	public static NonNullList<Ingredient> readIngredients(JsonArray jsonArray) {
		NonNullList<Ingredient> list = NonNullList.create();
		for (int i = 0; i < jsonArray.size(); i++) {
			Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
			if (!ingredient.isEmpty()) {
				list.add(ingredient);
			}
		}
		return list;
	}

	public static List<VariableOutput> readVariableProductionOutputs(JsonArray jsonArray) {
		List<VariableOutput> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			list.add(VariableOutput.deserialize(jsonArray.get(i).getAsJsonObject()));
		}
		return list;
	}

	public static List<IngredientStack> readIngredientStacks(JsonArray jsonArray) {
		List<IngredientStack> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); ++i) {
			IngredientStack ingredientStack = IngredientStack.fromJson(jsonArray.get(i).getAsJsonObject());
			if (!ingredientStack.ingredient().isEmpty()) {
				list.add(ingredientStack);
			}
		}
		return list;
	}

	public static List<Ingredient> flattenIngredientStacks(List<IngredientStack> ingredients) {
		List<Ingredient> flatIngredients = new ArrayList<>();
		for (IngredientStack ingredientStack : ingredients) {
			Ingredient ingredient = ingredientStack.ingredient();
			for (int i = 0; i < ingredientStack.count(); i++) {
				flatIngredients.add(ingredient);
			}
		}
		return flatIngredients;
	}

}
