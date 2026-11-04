package com.github.elenterius.biomancy.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.gui.tooltip.ScreenNutrientFuelConsumer;
import com.github.elenterius.biomancy.client.gui.tooltip.ScreenTooltipStyleProvider;
import com.github.elenterius.biomancy.client.util.GuiRenderUtil;
import com.github.elenterius.biomancy.client.util.GuiUtil;
import com.github.elenterius.biomancy.menu.DigesterMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DigesterScreen extends AbstractContainerScreen<DigesterMenu> implements ScreenTooltipStyleProvider, ScreenNutrientFuelConsumer {

	private static final Identifier BACKGROUND_TEXTURE = BiomancyMod.rl("textures/gui/menu_digester.png");

	public DigesterScreen(DigesterMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 176, 193);
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
		drawProgressBar(guiGraphics, menu.getCraftingProgressNormalized());
		drawFuelBar(guiGraphics, menu.getFuelAmountNormalized());
	}

	private void drawProgressBar(GuiGraphicsExtractor guiGraphics, float craftingPct) {
		int uWidth = (int) (craftingPct * 20) + (craftingPct > 0 ? 1 : 0);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos + 78, topPos + 17, 194, 0, uWidth, 2, 256, 256);
	}

	private void drawFuelBar(GuiGraphicsExtractor guiGraphics, float fuelPct) {
		int vHeight = (int) (fuelPct * 36) + (fuelPct > 0 ? 1 : 0);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos + 44, topPos + 28 + 36 - vHeight, 178, 36 - vHeight, 5, vHeight, 256, 256);

		int cost = menu.getFuelCost();
		if (cost <= menu.getFuelAmount()) return;

		int x = leftPos + 44;
		int y = topPos + 28 + 36 - ((int) ((cost / (float) menu.getMaxFuelAmount()) * 36) + 1);
		guiGraphics.fill(x - 3, y, x + 5 + 3, y + 1, 0x30_ff0000);
		guiGraphics.fill(x + 5 + 3, y, x + 5 + 3 + 1, y + 1, 0xff_ff0000);
		guiGraphics.fill(x + 5 + 3 + 1, y - 1, x + 5 + 3 + 2, y + 2, 0xff_ff0000);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (menu.getCarried().isEmpty() && (GuiUtil.isInRect(leftPos + 44, topPos + 26, 5, 36, mouseX, mouseY))) {
			drawFuelTooltip(guiGraphics, mouseX, mouseY);
			return;
		}

		super.extractTooltip(guiGraphics, mouseX, mouseY);
	}

	private void drawFuelTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int maxFuel = menu.getMaxFuelAmount();
		int fuelAmount = menu.getFuelAmount();
		int totalFuelCost = menu.getFuelCost();
		GuiRenderUtil.drawFuelTooltip(font, graphics, mouseX, mouseY, maxFuel, fuelAmount, totalFuelCost);
	}

}
