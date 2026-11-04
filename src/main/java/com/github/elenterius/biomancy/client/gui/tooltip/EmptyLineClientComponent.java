package com.github.elenterius.biomancy.client.gui.tooltip;

import com.github.elenterius.biomancy.tooltip.EmptyLineTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class EmptyLineClientComponent implements ClientTooltipComponent {

	public EmptyLineClientComponent(EmptyLineTooltipComponent component) {}

	@Override
	public int getHeight(Font font) {
		return 10;
	}

	@Override
	public int getWidth(Font font) {
		return 1;
	}

}
