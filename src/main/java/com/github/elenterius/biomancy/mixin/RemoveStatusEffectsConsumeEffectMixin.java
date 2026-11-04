package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.init.ModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RemoveStatusEffectsConsumeEffect.class)
public abstract class RemoveStatusEffectsConsumeEffectMixin {

	@Shadow
	@Final
	private HolderSet<MobEffect> effects;

	@Inject(method = "apply", at = @At("RETURN"), cancellable = true)
	private void onApply(Level level, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir) {
		boolean curesPoison = false;
		for (Holder<MobEffect> effect : effects) {
			if (effect.is(MobEffects.POISON)) {
				curesPoison = true;
				break;
			}
		}
		if (curesPoison && user.removeEffect(ModMobEffects.TOXIN)) {
			cir.setReturnValue(true);
		}
	}

}
