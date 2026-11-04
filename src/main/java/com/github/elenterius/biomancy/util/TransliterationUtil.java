package com.github.elenterius.biomancy.util;

import com.github.elenterius.biomancy.BiomancyConfig;
import com.ibm.icu.text.Transliterator;
import net.minecraft.network.chat.Component;

import java.util.function.UnaryOperator;

public final class TransliterationUtil {
	private static final String ANY_TO_LATIN_WITHOUT_ACCENTS_TO_ASCII = "Any-Latin; NFD; [:Nonspacing Mark:] Remove; NFC; Latin-ASCII;";

	private static Transliterator ASCII_TRANSLITERATOR;

	private TransliterationUtil() {}

	public static void init() {
		ASCII_TRANSLITERATOR = Transliterator.getInstance(ANY_TO_LATIN_WITHOUT_ACCENTS_TO_ASCII);
	}

	public static String transliterate(String text) {
		if (!BiomancyConfig.SERVER_SYNCED.doTransliteration.get()) return text;
		if (text.isBlank()) return text;
		return ASCII_TRANSLITERATOR.transliterate(text);
	}

	public static Component transliterate(Component component, UnaryOperator<String> resultModifier) {
		if (!BiomancyConfig.SERVER_SYNCED.doTransliteration.get()) return component;

		String original = component.getString();
		String transliterated = transliterate(original);

		if (transliterated.equals(original)) return component;

		return ComponentUtil.literal(resultModifier.apply(transliterated)).setStyle(component.getStyle());
	}

}
