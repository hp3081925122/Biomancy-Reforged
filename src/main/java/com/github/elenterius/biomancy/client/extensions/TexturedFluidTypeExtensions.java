package com.github.elenterius.biomancy.client.extensions;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class TexturedFluidTypeExtensions implements IClientFluidTypeExtensions {

	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;
	private final ResourceLocation blockOverlayTexture;
	private final ResourceLocation screenOverlayTexture;

	public TexturedFluidTypeExtensions(String name) {
		stillTexture = BiomancyMod.rl("block/%s_still".formatted(name));
		flowingTexture = BiomancyMod.rl("block/%s_flowing".formatted(name));
		blockOverlayTexture = BiomancyMod.rl("block/%s_overlay".formatted(name));
		screenOverlayTexture = BiomancyMod.rl("textures/block/%s_overlay.png".formatted(name));
	}

	@Override
	public ResourceLocation getStillTexture() {
		return stillTexture;
	}

	@Override
	public ResourceLocation getFlowingTexture() {
		return flowingTexture;
	}

	@Override
	public ResourceLocation getOverlayTexture() {
		return blockOverlayTexture;
	}

	@Override
	public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
		return screenOverlayTexture;
	}

}
