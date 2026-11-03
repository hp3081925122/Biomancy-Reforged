package com.github.elenterius.biomancy.mixin.client;

import com.github.elenterius.biomancy.client.gui.tooltip.TooltipHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

	@Shadow
	private ItemStack tooltipStack;

	@Inject(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER), require = 0)
	private void onPostRenderTooltipInternal(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci,
											 @Local(ordinal = 2) int width, @Local(ordinal = 3) int height, @Local Vector2ic pos) {
		GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
		TooltipHandler.onPostRenderTooltip(tooltipStack, components, font, guiGraphics, pos.x(), pos.y(), width, height);
	}

}
