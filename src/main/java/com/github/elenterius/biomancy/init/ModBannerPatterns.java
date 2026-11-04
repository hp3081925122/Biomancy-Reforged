package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public final class ModBannerPatterns {

	public static final ResourceKey<BannerPattern> MASCOT_BASE = create("mascot_base");
	public static final ResourceKey<BannerPattern> MASCOT_ACCENT = create("mascot_accent");
	public static final ResourceKey<BannerPattern> MASCOT_OUTLINE = create("mascot_outline");
	public static final TagKey<BannerPattern> TAG_MASCOT = createTagKey("mascot");

	private ModBannerPatterns() {}

	public static void bootstrap(BootstrapContext<BannerPattern> context) {
		register(context, MASCOT_BASE);
		register(context, MASCOT_ACCENT);
		register(context, MASCOT_OUTLINE);
	}

	private static void register(BootstrapContext<BannerPattern> context, ResourceKey<BannerPattern> key) {
		context.register(key, new BannerPattern(key.identifier(), "block.minecraft.banner." + key.identifier().toShortLanguageKey()));
	}

	private static ResourceKey<BannerPattern> create(String name) {
		return ResourceKey.create(Registries.BANNER_PATTERN, BiomancyMod.rl(name));
	}

	private static TagKey<BannerPattern> createTagKey(String name) {
		return TagKey.create(Registries.BANNER_PATTERN, BiomancyMod.rl("pattern_item/" + name));
	}

	private static TagKey<BannerPattern> createTagKey(ResourceKey<BannerPattern> key) {
		return TagKey.create(Registries.BANNER_PATTERN, key.identifier().withPrefix("pattern_item/"));
	}

}
