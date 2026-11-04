package com.github.elenterius.biomancy.client.gui;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.gui.tooltip.ScreenTooltipStyleProvider;
import com.github.elenterius.biomancy.menu.StorageSacMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class StorageSacScreen extends AbstractContainerScreen<StorageSacMenu> implements ScreenTooltipStyleProvider {

	private static final Identifier BACKGROUND_TEXTURE = BiomancyMod.rl("textures/gui/menu_storage_sac.png");

	public StorageSacScreen(StorageSacMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 176, 170);
	}

	@Override
	protected void init() {
		super.init();
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
