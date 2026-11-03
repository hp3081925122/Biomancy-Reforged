package com.github.elenterius.biomancy.init;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

public final class ModRarities {

	public static final Rarity COMMON = Rarity.valueOf("BIOMANCY_COMMON");
	public static final Rarity UNCOMMON = Rarity.valueOf("BIOMANCY_UNCOMMON");
	public static final Rarity RARE = Rarity.valueOf("BIOMANCY_RARE");
	public static final Rarity VERY_RARE = Rarity.valueOf("BIOMANCY_VERY_RARE");
	public static final Rarity ULTRA_RARE = Rarity.valueOf("BIOMANCY_ULTRA_RARE");

	private ModRarities() {}

	public static int getRGBColor(ItemStack stack) {
		TextColor color = stack.getRarity().getStyleModifier().apply(Style.EMPTY).getColor();
		return color != null ? color.getValue() : 0xFF_FF_FF;
	}

	public static int getARGBColor(ItemStack stack) {
		return getRGBColor(stack) | 0xFF_00_00_00;
	}

	public static final class Proxies {
		public static final EnumProxy<Rarity> COMMON = create("common", 0xA58369);
		public static final EnumProxy<Rarity> UNCOMMON = create("uncommon", 0xB19748);
		public static final EnumProxy<Rarity> RARE = create("rare", 0x2E9E3E);
		public static final EnumProxy<Rarity> VERY_RARE = create("very_rare", 0xA870E1);
		public static final EnumProxy<Rarity> ULTRA_RARE = create("ultra_rare", 0xFF3D51);

		private Proxies() {}

		private static EnumProxy<Rarity> create(String name, int rgbColor) {
			return new EnumProxy<>(Rarity.class, -1, "biomancy:" + name, (UnaryOperator<Style>) style -> style.withColor(rgbColor));
		}
	}

}
