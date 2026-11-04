package com.github.elenterius.biomancy.client.extensions;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class TexturedFluidTypeExtensions implements IClientFluidTypeExtensions {

	private final Identifier screenOverlayTexture;

	public TexturedFluidTypeExtensions(String name) {
		screenOverlayTexture = BiomancyMod.rl("textures/block/%s_overlay.png".formatted(name));
	}

	public static FluidModel.Unbaked createTranslucentFluidModel(String name) {
		return new FluidModel.Unbaked(
				new Material(BiomancyMod.rl("block/%s_still".formatted(name)), true),
				new Material(BiomancyMod.rl("block/%s_flowing".formatted(name)), true),
				new Material(BiomancyMod.rl("block/%s_overlay".formatted(name)), true),
				null
		);
	}

	@Override
	public Identifier getRenderOverlayTexture(Minecraft mc) {
		return screenOverlayTexture;
	}

}
