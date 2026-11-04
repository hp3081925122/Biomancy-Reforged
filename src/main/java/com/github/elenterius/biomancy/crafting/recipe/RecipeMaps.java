package com.github.elenterius.biomancy.crafting.recipe;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class RecipeMaps {

	private static volatile RecipeMap clientRecipeMap = RecipeMap.EMPTY;

	private RecipeMaps() {}

	public static RecipeMap get(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.recipeAccess().recipeMap();
		}
		return clientRecipeMap;
	}

	public static RecipeMap getClientRecipeMap() {
		return clientRecipeMap;
	}

	public static void setClientRecipeMap(RecipeMap recipeMap) {
		clientRecipeMap = recipeMap;
	}

	@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event) {
		event.sendRecipes(
				ModRecipes.DECOMPOSING_RECIPE_TYPE.get(),
				ModRecipes.BIO_BREWING_RECIPE_TYPE.get(),
				ModRecipes.BIO_FORGING_RECIPE_TYPE.get(),
				ModRecipes.DIGESTING_RECIPE_TYPE.get()
		);
	}

}
