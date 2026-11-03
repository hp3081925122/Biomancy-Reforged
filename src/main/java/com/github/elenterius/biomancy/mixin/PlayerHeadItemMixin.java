package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.BiomancyMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(PlayerHeadItem.class)
public abstract class PlayerHeadItemMixin {

	@Inject(method = "verifyComponentsAfterLoad", at = @At(value = "TAIL"))
	private void onVerifyComponentsAfterLoad(ItemStack stack, CallbackInfo ci) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null || !customData.contains(BiomancyMod.MOD_ID)) return;

		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		if (profile == null) return;

		CompletableFuture<Optional<GameProfile>> future;
		if (profile.name().isPresent()) future = SkullBlockEntity.fetchGameProfile(profile.name().get());
		else if (profile.id().isPresent()) future = SkullBlockEntity.fetchGameProfile(profile.id().get());
		else return;

		future.thenAcceptAsync(fetched -> {
			fetched.ifPresent(gameProfile -> stack.set(DataComponents.PROFILE, new ResolvableProfile(gameProfile)));
			CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(BiomancyMod.MOD_ID));
		}, SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
	}

}
