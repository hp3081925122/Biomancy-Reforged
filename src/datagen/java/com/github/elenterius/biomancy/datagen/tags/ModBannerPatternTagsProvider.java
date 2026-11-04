package com.github.elenterius.biomancy.datagen.tags;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModBannerPatterns;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBannerPatternTagsProvider extends KeyTagProvider<BannerPattern> {

	public ModBannerPatternTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.BANNER_PATTERN, lookupProvider, BiomancyMod.MOD_ID);
	}

	@Override
	public String getName() {
		return StringUtils.capitalize(modId) + " " + super.getName();
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(ModBannerPatterns.TAG_MASCOT).add(
				ModBannerPatterns.MASCOT_BASE,
				ModBannerPatterns.MASCOT_ACCENT,
				ModBannerPatterns.MASCOT_OUTLINE
		);
	}

}
