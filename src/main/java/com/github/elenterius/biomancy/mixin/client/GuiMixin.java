package com.github.elenterius.biomancy.mixin.client;

import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.item.CrosshairProvider;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
	private void onRenderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;

		ItemStack itemStack = minecraft.player.getMainHandItem();

		if (!itemStack.isEmpty() && itemStack.getItem() instanceof CrosshairProvider provider) {
			ci.cancel();

			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			int screenWidth = guiGraphics.guiWidth();
			int screenHeight = guiGraphics.guiHeight();
			int r = CrosshairProvider.TEXTURE_SIZE;
			ResourceLocation texture = provider.getCrosshairTexture(itemStack, minecraft.player);
			guiGraphics.blit(texture, (screenWidth - r) / 2 + (screenWidth % 2), (screenHeight - r) / 2, 0, 0, r, r, r, r);
		}
	}

	@ModifyExpressionValue(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
	private boolean canRenderFoodHunger(boolean hasHungerEffect, GuiGraphics guiGraphics, Player player, int y, int x) {
		if (!hasHungerEffect && player.hasEffect(ModMobEffects.WITHDRAWAL)) {
			return true;
		}
		return hasHungerEffect;
	}

}
