package com.github.elenterius.biomancy.styles;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.item.KnowledgeReader;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class Fonts {

	//	public static final Identifier ILLAGER_RUNES = new Identifier("minecraft", "illageralt");
	//	public static final Identifier STANDARD_GALACTIC_ALPHABET = new Identifier("minecraft", "alt");

	private Fonts() {}

	public static final class PrimordialRunes {

		public static final Identifier CARO_INVITICA = BiomancyMod.rl("caro_invitica");
		public static final FontDescription.Resource FONT = new FontDescription.Resource(CARO_INVITICA);
		public static final int GLYPH_WIDTH = 8;

		/// only updated on the client side
		private static boolean isTranslatable = false;

		private PrimordialRunes() {}

		public static FontDescription getId() {
			return FONT;
		}

		public static boolean isTranslatable(Identifier font) {
			return isTranslatable && font.equals(CARO_INVITICA);
		}

		/// needs to be called on the client side
		public static void updateTranslatable(Player player) {
			isTranslatable = player.hasEffect(ModMobEffects.PRIMORDIAL_INFESTATION) || KnowledgeReader.canTranslatePrimordialRunes(player, EquipmentSlot.HEAD);
		}

	}

}
