package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.serum.InsomniaCureSerum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Phantom.class)
public abstract class PhantomMixin {

	@Inject(
			method = "canAttack(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z",
			at = @At(value = "HEAD"), cancellable = true
	)
	private void onCanAttack(ServerLevel level, LivingEntity target, TargetingConditions condition, CallbackInfoReturnable<Boolean> cir) {
		Phantom attacker = (Phantom) (Object) this;
		CompoundTag data = target.getPersistentData();
		if (data.contains(InsomniaCureSerum.DATA_KEY)) {
			long elapsedTime = attacker.level().getGameTime() - data.getLongOr(InsomniaCureSerum.DATA_KEY, 0L);
			if (elapsedTime < InsomniaCureSerum.PROTECTION_TICKS) {
				if (elapsedTime < InsomniaCureSerum.PROTECTION_TICKS / 2 && attacker.getRandom().nextFloat() < 0.25f) {
					attacker.hurt(attacker.level().damageSources().magic(), 1f);
				}
				cir.setReturnValue(false);
			}
			else {
				data.remove(InsomniaCureSerum.DATA_KEY);
			}
		}
	}

}
