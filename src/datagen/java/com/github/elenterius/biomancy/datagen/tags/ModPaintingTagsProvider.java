package com.github.elenterius.biomancy.datagen.tags;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModPaintings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModPaintingTagsProvider extends KeyTagProvider<PaintingVariant> {

	public ModPaintingTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.PAINTING_VARIANT, lookupProvider, BiomancyMod.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider pProvider) {
		tag(PaintingVariantTags.PLACEABLE)
				.add(ModPaintings.JERRY_PROVIDES);
	}

}
