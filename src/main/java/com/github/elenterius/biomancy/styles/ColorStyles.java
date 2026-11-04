package com.github.elenterius.biomancy.styles;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModRarities;
import com.github.elenterius.biomancy.item.ItemTooltipStyleProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.Set;

public final class ColorStyles {

	public static final int WHITE_ARGB = 0xFF_FFFFFF;
	public static final int BLACK_ARGB = 0xFF_000000;

	public static final int TOOLTIP_BACKGROUND_ARGB = 0xF2_040404;
	public static final int TOOLTIP_BORDER_ARGB = 0xFF_903E55;

	public static final int NUTRIENTS_FUEL_BAR = 0x94A856;

	public static final int TEXT_NUTRIENTS = 0x65b52a;
	public static final int TEXT_NUTRIENTS_CONSUMPTION = 0xe7bd42;
	public static final int TEXT_ERROR = 0xC12727;
	public static final int TEXT_SUCCESS = 0x65B52A;
	public static final int TEXT_ACCENT_FORGE = 0xA88773;
	public static final int TEXT_ACCENT_FORGE_DARK = 0x51423A;
	public static final int TEXT_MUTED_AQUA = 0x459393;

	private static final Set<Integer> TOOLTIP_SPRITE_COLORS = Set.of(0x903E55, 0xA58369, 0xB19748, 0x2E9E3E, 0xA870E1, 0xFF3D51, 0xFFFF55, 0x55FFFF, 0xFF55FF, 0xAA00AA, 0x55FF55, 0xFF5555, 0x5555FF, 0xFFAA00);

	public static final TooltipStyle GENERIC_TOOLTIP = new TooltipStyle(TOOLTIP_BACKGROUND_ARGB, TOOLTIP_BORDER_ARGB, TOOLTIP_BORDER_ARGB);
	public static final ITooltipStyle CUSTOM_RARITY_TOOLTIP = tooltipEvent -> {
		ItemStack stack = tooltipEvent.getItemStack();
		int rarityColor = stack.getItem() instanceof ItemTooltipStyleProvider styleProvider ? styleProvider.getTooltipColorARGB(stack) : ModRarities.getARGBColor(stack);

		if (rarityColor != WHITE_ARGB && TOOLTIP_SPRITE_COLORS.contains(rarityColor & 0xFF_FFFF)) {
			tooltipEvent.setTexture(tooltipSprite(rarityColor));
		}
		else {
			GENERIC_TOOLTIP.applyColorTo(tooltipEvent);
		}
	};

	private ColorStyles() {}

	public interface ITooltipStyle {
		void applyColorTo(final RenderTooltipEvent.Texture tooltipEvent);
	}

	public static Identifier tooltipSprite(int borderColor) {
		return BiomancyMod.rl("c_%06x".formatted(borderColor & 0xFF_FFFF));
	}

	public record TooltipStyle(int backgroundColor, int borderStartColor, int borderEndColor) implements ITooltipStyle {
		@Override
		public void applyColorTo(final RenderTooltipEvent.Texture tooltipEvent) {
			tooltipEvent.setTexture(tooltipSprite(borderStartColor));
		}
	}

}
