package com.github.elenterius.biomancy.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class TintedFluidTypeExtensions implements IClientFluidTypeExtensions {

	protected static final Identifier STILL_TEXTURE = Identifier.withDefaultNamespace("block/water_still");
	protected static final Identifier FLOWING_TEXTURE = Identifier.withDefaultNamespace("block/water_flow");
	protected static final Identifier OVERLAY_TEXTURE = Identifier.withDefaultNamespace("block/water_overlay");
	protected static final Identifier INSIDE_FLUID_TEXTURE = Identifier.withDefaultNamespace("textures/block/water_overlay.png");

	protected final int colorARGB;

	public TintedFluidTypeExtensions(int colorARGB) {
		this.colorARGB = colorARGB;
	}

	public static FluidModel.Unbaked createFluidModel(int colorARGB) {
		return new FluidModel.Unbaked(new Material(STILL_TEXTURE), new Material(FLOWING_TEXTURE), new Material(OVERLAY_TEXTURE), FluidTintSources.constant(colorARGB));
	}

	public static void renderTintedScreenOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource buffers, Identifier texture, int colorARGB) {
		LocalPlayer player = minecraft.player;

		BlockPos blockPos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
		float brightness = Lightmap.getBrightness(player.level().dimensionType(), player.level().getMaxLocalRawBrightness(blockPos));
		float red = (ARGB.red(colorARGB) / 255f) * brightness;
		float green = (ARGB.green(colorARGB) / 255f) * brightness;
		float blue = (ARGB.blue(colorARGB) / 255f) * brightness;
		int color = ARGB.colorFromFloat(0.5f, red, green, blue);

		float uOffset = -player.getYRot() / 64f;
		float vOffset = player.getXRot() / 64f;
		float depth = -0.5f;

		Matrix4f matrix4f = poseStack.last().pose();
		VertexConsumer consumer = buffers.getBuffer(RenderTypes.blockScreenEffect(texture));
		consumer.addVertex(matrix4f, -1f, -1f, depth).setUv(4f + uOffset, 4f + vOffset).setColor(color);
		consumer.addVertex(matrix4f, 1f, -1f, depth).setUv(uOffset, 4f + vOffset).setColor(color);
		consumer.addVertex(matrix4f, 1f, 1f, depth).setUv(uOffset, vOffset).setColor(color);
		consumer.addVertex(matrix4f, -1f, 1f, depth).setUv(4f + uOffset, vOffset).setColor(color);
	}

	@Override
	public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
		float red = (ARGB.red(colorARGB) / 255f) * fluidFogColor.x();
		float green = (ARGB.green(colorARGB) / 255f) * fluidFogColor.y();
		float blue = (ARGB.blue(colorARGB) / 255f) * fluidFogColor.z();
		fluidFogColor.set(red, green, blue, fluidFogColor.w());
	}

	@Override
	public Identifier getRenderOverlayTexture(Minecraft mc) {
		return INSIDE_FLUID_TEXTURE;
	}

	@Override
	public void renderOverlay(Minecraft mc, PoseStack poseStack, MultiBufferSource buffers) {
		renderTintedScreenOverlay(mc, poseStack, buffers, INSIDE_FLUID_TEXTURE, colorARGB);
	}

}
