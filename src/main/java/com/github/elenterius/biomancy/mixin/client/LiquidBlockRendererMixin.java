package com.github.elenterius.biomancy.mixin.client;

import com.github.elenterius.biomancy.block.WaterGelBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class LiquidBlockRendererMixin {

	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	private void onTessellate(BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		if (blockState.getBlock() instanceof WaterGelBlock) {
			ci.cancel();
		}
	}

}
