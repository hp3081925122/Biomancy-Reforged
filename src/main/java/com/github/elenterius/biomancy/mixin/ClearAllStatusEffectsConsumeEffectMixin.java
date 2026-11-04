package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.statuseffect.StatusEffectHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClearAllStatusEffectsConsumeEffect.class)
public abstract class ClearAllStatusEffectsConsumeEffectMixin {

	@Inject(method = "apply", at = @At("HEAD"), cancellable = true)
	private void onApply(Level level, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(StatusEffectHandler.clearEffectsCuredBy(user, stack));
	}

}
