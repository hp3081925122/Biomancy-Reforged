package com.github.elenterius.biomancy.client.gui.tooltip;

import com.github.elenterius.biomancy.tooltip.HrTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class HrTooltipClientComponent implements ClientTooltipComponent {

	private final int colorARGB;
	private int lineNumber;

	public HrTooltipClientComponent(HrTooltipComponent component) {
		this.colorARGB = component.colorARGB();
	}

	void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public int getHeight(Font font) {
		return 8;
	}

	@Override
	public int getWidth(Font font) {
		return 1;
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
		int yOffset = lineNumber == 1 ? 0 : 2;
		guiGraphics.fill(x, y + yOffset, x + width, y + yOffset + 4, colorARGB);
	}

}
