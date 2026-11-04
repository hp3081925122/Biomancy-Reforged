package com.github.elenterius.biomancy.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.Optional;
import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

public final class ModPaintings {

	public static final ResourceKey<PaintingVariant> JERRY_PROVIDES = create("jerry_provides");

	private ModPaintings() {}

	public static void bootstrap(BootstrapContext<PaintingVariant> context) {
		register(context, JERRY_PROVIDES, 1, 1);
	}

	private static void register(BootstrapContext<PaintingVariant> context, ResourceKey<PaintingVariant> key, int width, int height) {
		context.register(key, new PaintingVariant(width, height, key.identifier(), Optional.of(Component.translatable(key.identifier().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW)), Optional.of(Component.translatable(key.identifier().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY))));
	}

	private static ResourceKey<PaintingVariant> create(String name) {
		return ResourceKey.create(Registries.PAINTING_VARIANT, BiomancyMod.rl(name));
	}

}
