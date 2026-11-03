package com.github.elenterius.biomancy.init.client;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.recipe.BioForgingRecipe;
import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModRecipeBookTypes;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;

import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class ModRecipeBookCategories {

	public static final RecipeBookCategories BIO_FORGE = RecipeBookCategories.valueOf("BIOMANCY_BIO_FORGE");

	private ModRecipeBookCategories() {}

	public static boolean isInTab(RecipeCollection recipeCollection, BioForgeTab tab) {
		if (tab == ModBioForgeTabs.SEARCH.get()) return true;
		return recipeCollection.getRecipes().stream().anyMatch(holder -> holder.value() instanceof BioForgingRecipe recipe && recipe.getTab() == tab);
	}

	@SubscribeEvent
	public static void registerRecipeBooks(RegisterRecipeBookCategoriesEvent event) {
		event.registerBookCategories(ModRecipeBookTypes.BIO_FORGE, List.of(BIO_FORGE));
		event.registerRecipeCategoryFinder(ModRecipes.BIO_FORGING_RECIPE_TYPE.get(), holder -> holder.value() instanceof BioForgingRecipe ? BIO_FORGE : null);

		event.registerRecipeCategoryFinder(ModRecipes.BIO_BREWING_RECIPE_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);
		event.registerRecipeCategoryFinder(ModRecipes.DECOMPOSING_RECIPE_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);
		event.registerRecipeCategoryFinder(ModRecipes.DIGESTING_RECIPE_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);
	}

	public static final class Proxies {
		public static final EnumProxy<RecipeBookCategories> BIO_FORGE = new EnumProxy<>(RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(ModBioForgeTabs.SEARCH.get().getIcon()));

		private Proxies() {}
	}

}
