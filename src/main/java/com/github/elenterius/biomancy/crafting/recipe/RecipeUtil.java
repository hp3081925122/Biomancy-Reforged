package com.github.elenterius.biomancy.crafting.recipe;

import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import net.minecraft.world.item.crafting.Ingredient;

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
