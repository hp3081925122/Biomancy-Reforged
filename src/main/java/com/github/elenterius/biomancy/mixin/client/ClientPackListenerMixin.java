package com.github.elenterius.biomancy.mixin.client;

import com.github.elenterius.biomancy.client.gui.BioForgeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPackListenerMixin {

	@Inject(method = "handleRecipeBookAdd", at = @At(value = "TAIL"))
	private void onRecipeBookAdd(ClientboundRecipeBookAddPacket packet, CallbackInfo ci) {
		if (Minecraft.getInstance().screen instanceof BioForgeScreen screen) screen.onRecipeBookUpdated();
	}

	@Inject(method = "handleRecipeBookRemove", at = @At(value = "TAIL"))
	private void onRecipeBookRemove(ClientboundRecipeBookRemovePacket packet, CallbackInfo ci) {
		if (Minecraft.getInstance().screen instanceof BioForgeScreen screen) screen.onRecipeBookUpdated();
	}

}
