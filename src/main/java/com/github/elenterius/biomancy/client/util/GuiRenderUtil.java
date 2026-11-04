package com.github.elenterius.biomancy.client.util;

import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.biomancy.util.FormatUtil;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class GuiRenderUtil {

	protected static final Identifier ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background");
	protected static final Identifier ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");

	private GuiRenderUtil() {}

	public static void drawFuelTooltip(Font font, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int maxFuel, int fuelAmount, int totalFuelCost) {
		List<Component> hoveringText = new ArrayList<>();
		DecimalFormat df = FormatUtil.getIntegerFormatter();

		hoveringText.add(ComponentUtil.translatable("tooltip.biomancy.nutrients_fuel").withStyle(TextStyles.NUTRIENTS));
		hoveringText.add(ComponentUtil.literal("%s/%s u".formatted(df.format(fuelAmount), df.format(maxFuel))));
		if (totalFuelCost > 0) {
			hoveringText.add(ComponentUtil.translatable("tooltip.biomancy.nutrients_consumes", df.format(totalFuelCost)).withStyle(TextStyles.NUTRIENTS_CONSUMPTION));
		}

		guiGraphics.setComponentTooltipForNextFrame(font, hoveringText, mouseX, mouseY);
	}

	public static void drawAttackIndicator(GuiGraphicsExtractor guiGraphics, int x, int y, float pct) {
		guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 16, 4);
		guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, x, y, (int) (pct * 17f), 4);
	}

	public static void drawSquareProgressBar(GuiGraphicsExtractor guiGraphics, int centerX, int centerY, int blitOffset, int radius, float pct) {
		drawSquareProgressBar(guiGraphics, centerX, centerY, radius, blitOffset, pct, 0xFF_FFFFFF);
	}

	private static void drawSquareProgressBar(GuiGraphicsExtractor guiGraphics, int centerX, int centerY, int radius, int blitOffset, float pct, int argbColor) {
		int size = 1;
		int sideLength = radius * 2;
		int halfLength = radius;
		int currentLength = Math.round(pct * (sideLength * 4));

		int dist = Math.min(currentLength, halfLength);
		if (dist > 0) {
			int y = centerY - halfLength;
			drawQuad(guiGraphics, centerX, y - size, centerX + halfLength + 1, y + size, blitOffset, argbColor);
		}

		dist = Math.min(currentLength - halfLength, sideLength);
		if (dist > 0) {
			int x = centerX + halfLength;
			int y = centerY - halfLength;
			drawQuad(guiGraphics, x - size + 1, y, x + size + 1, y + dist, blitOffset, argbColor);
		}

		dist = Math.min(currentLength - 3 * halfLength, sideLength);
		if (dist > 0) {
			int x = centerX + halfLength;
			int y = centerY + halfLength;
			drawQuad(guiGraphics, x - dist, y - size, x, y + size, blitOffset, argbColor);
		}

		dist = Math.min(currentLength - 5 * halfLength, sideLength);
		if (dist > 0) {
			int x = centerX - halfLength;
			int y = centerY + halfLength;
			drawQuad(guiGraphics, x - size, y - dist, x + size, y, blitOffset, argbColor);
		}

		dist = Math.min(currentLength - 7 * halfLength, halfLength);
		if (dist > 0) {
			int x = centerX - halfLength;
			int y = centerY - halfLength;
			drawQuad(guiGraphics, x, y - size, x + halfLength, y + size, blitOffset, argbColor);
		}

	}

	private static void drawQuad(GuiGraphicsExtractor guiGraphics, int minX, int minY, int maxX, int maxY, int z, int argbColor) {
		if (minX < maxX) {
			int n = minX;
			minX = maxX;
			maxX = n;
		}

		if (minY < maxY) {
			int n = minY;
			minY = maxY;
			maxY = n;
		}

		guiGraphics.fill(RenderPipelines.GUI_INVERT, maxX, maxY, minX, minY, argbColor);
	}

	public static void fillQuad(GuiGraphicsExtractor guiGraphics, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int colorARGB) {
		Matrix3x2f pose = new Matrix3x2f(guiGraphics.pose());
		ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
		int minX = Mth.floor(Math.min(Math.min(x0, x1), Math.min(x2, x3)));
		int minY = Mth.floor(Math.min(Math.min(y0, y1), Math.min(y2, y3)));
		int maxX = Mth.ceil(Math.max(Math.max(x0, x1), Math.max(x2, x3)));
		int maxY = Mth.ceil(Math.max(Math.max(y0, y1), Math.max(y2, y3)));
		ScreenRectangle bounds = new ScreenRectangle(minX, minY, maxX - minX, maxY - minY).transformMaxBounds(pose);
		if (scissorArea != null) bounds = scissorArea.intersection(bounds);
		guiGraphics.submitGuiElementRenderState(new ColoredQuadRenderState(pose, x0, y0, x1, y1, x2, y2, x3, y3, colorARGB, scissorArea, bounds));
	}

	private record ColoredQuadRenderState(Matrix3x2fc pose, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int color,
										  @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

		@Override
		public void buildVertices(VertexConsumer vertexConsumer) {
			vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(color);
			vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(color);
			vertexConsumer.addVertexWith2DPose(pose, x2, y2).setColor(color);
			vertexConsumer.addVertexWith2DPose(pose, x3, y3).setColor(color);
		}

		@Override
		public RenderPipeline pipeline() {
			return RenderPipelines.GUI;
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.noTexture();
		}

	}

	public static void fill(GuiGraphicsExtractor guiGraphics, float minX, float minY, float maxX, float maxY, int z, int colorARGB) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(minX, minY);
		guiGraphics.pose().scale(maxX - minX, maxY - minY);
		guiGraphics.fill(0, 0, 1, 1, colorARGB);
		guiGraphics.pose().popMatrix();
	}

}
