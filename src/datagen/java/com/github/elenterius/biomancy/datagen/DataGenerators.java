package com.github.elenterius.biomancy.datagen;

import net.minecraft.server.packs.PackType;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.datagen.advancements.ModAdvancementProvider;
import com.github.elenterius.biomancy.datagen.lang.EnglishLangProvider;
import com.github.elenterius.biomancy.datagen.loot.ModGlobalLootModifierProvider;
import com.github.elenterius.biomancy.datagen.loot.ModLootTableProvider;
import com.github.elenterius.biomancy.datagen.loot.WikiDespoilLootProvider;
import com.github.elenterius.biomancy.datagen.particles.ModParticleSpriteProvider;
import com.github.elenterius.biomancy.datagen.recipes.ModRecipeProvider;
import com.github.elenterius.biomancy.datagen.recipes.WikiRecipesProvider;
import com.github.elenterius.biomancy.datagen.tags.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.CommonHooks;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class DataGenerators {

	private DataGenerators() {}

	@SubscribeEvent
	public static void gatherData(final GatherDataEvent.Client event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		CommonHooks.markComponentClassAsValid(HolderSet.emptyNamed(BuiltInRegistries.ITEM, ItemTags.BANNERS).getClass());
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookupProvider.join()).forEach(DataComponentInitializers.PendingComponents::apply);

		DatapackEntriesProvider datapackEntriesProvider = new DatapackEntriesProvider(packOutput, lookupProvider);
		lookupProvider = datapackEntriesProvider.getRegistryProvider();
		generator.addProvider(true, datapackEntriesProvider);

		generator.addProvider(true, new ModBlockTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModEntityTypeTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModBannerPatternTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModDamageTypeTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModMobEffectTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModPaintingTagsProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModEnchantmentTagsProvider(packOutput, lookupProvider));

		ModRecipeProvider recipeProvider = new ModRecipeProvider(packOutput, lookupProvider);
		generator.addProvider(true, recipeProvider);
		generator.addProvider(true, new WikiRecipesProvider(packOutput, recipeProvider, lookupProvider));

		generator.addProvider(true, new ModDataMapProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModLootTableProvider(packOutput, lookupProvider));
		generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));
		if (System.getProperty("wiki.docs") != null) {
			generator.addProvider(true, new WikiDespoilLootProvider(packOutput, lookupProvider));
		}

		generator.addProvider(true, new ModSoundProvider(packOutput));

		generator.addProvider(true, new ModParticleSpriteProvider(packOutput, event.getResourceManager(PackType.CLIENT_RESOURCES)));

		EnglishLangProvider enLanguage = new EnglishLangProvider(packOutput);

		generator.addProvider(true, new ModAdvancementProvider(packOutput, lookupProvider, enLanguage));

		generator.addProvider(true, enLanguage);
	}

}
