package com.github.elenterius.biomancy.client.gui.tooltip;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.inventory.ItemHandlerWrapper;
import com.github.elenterius.biomancy.tooltip.StorageSacTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class StorageSacTooltipClientComponent implements ClientTooltipComponent {

	public static final Identifier TEXTURE = BiomancyMod.rl("textures/gui/tooltip/embedded/storage_sac_slots.png");
	private static final int OFFSET = 2;
	private final @Nullable ItemHandlerWrapper itemHandler;
	private final boolean isStorageSacEmpty;

	public StorageSacTooltipClientComponent(StorageSacTooltipComponent tooltip) {
		itemHandler = tooltip.getItemHandler();
		isStorageSacEmpty = itemHandler == null || itemHandler.isEmpty();
	}

	@Override
	public int getHeight(Font font) {
		return isStorageSacEmpty ? 0 : 56 + OFFSET * 2;
	}

	@Override
	public int getWidth(Font font) {
		return isStorageSacEmpty ? 0 : 92;
	}

	@Override
	public void extractImage(Font font, int posX, int posY, int width, int height, GuiGraphicsExtractor guiGraphics) {
		if (itemHandler == null || isStorageSacEmpty) return;

		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, posX, posY + OFFSET, 0, 0, 92, 56, 128, 64);

		boolean drawHighlight = true;
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (stack.isEmpty()) continue;

			int x = posX + 2 + 18 * (i % 5);
			int y = posY + OFFSET + 2 + 18 * (i / 5);
			guiGraphics.item(stack, x, y, i);
			guiGraphics.itemDecorations(font, stack, x, y);
			if (drawHighlight) {
				guiGraphics.fill(x, y, x + 16, y + 16, 0x80_FFFFFF);
				drawHighlight = false;
			}
		}
	}

}
