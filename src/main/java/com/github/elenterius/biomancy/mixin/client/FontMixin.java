package com.github.elenterius.biomancy.mixin.client;

import com.github.elenterius.biomancy.styles.Fonts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.network.chat.FontDescription;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontMixin {

	@Shadow
	@Final
	private Font.Provider provider;

	@Inject(method = "getGlyphSource", at = @At(value = "HEAD"), cancellable = true)
	private void onGetGlyphSource(FontDescription fontLocation, CallbackInfoReturnable<GlyphSource> cir) {
		if (fontLocation instanceof FontDescription.Resource resource && Fonts.PrimordialRunes.isTranslatable(resource.id())) {
			cir.setReturnValue(provider.glyphs(FontDescription.DEFAULT));
		}
	}

}
