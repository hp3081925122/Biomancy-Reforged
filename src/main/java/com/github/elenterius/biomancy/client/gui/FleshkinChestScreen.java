package com.github.elenterius.biomancy.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.gui.tooltip.ScreenTooltipStyleProvider;
import com.github.elenterius.biomancy.menu.FleshkinChestMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FleshkinChestScreen extends AbstractContainerScreen<FleshkinChestMenu> implements ScreenTooltipStyleProvider {

	private static final Identifier BACKGROUND_TEXTURE = BiomancyMod.rl("textures/gui/menu_fleshkin_chest.png");

	public FleshkinChestScreen(FleshkinChestMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 176, 232);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

}
