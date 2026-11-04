package com.github.elenterius.biomancy.client.util;

import com.geckolib.loading.math.MolangQueries;
import net.minecraft.world.entity.LivingEntity;

public final class MolangUtil {

	private MolangUtil() {}

	public static void registerVariables() {
		MolangQueries.<Object>setActorVariable("variable.limb_swing", actor -> {
			if (!(actor.animatable() instanceof LivingEntity entity) || !canAnimateLimbs(entity)) return 0;
			float limbSwing = entity.walkAnimation.position(actor.partialTick());
			if (entity.isBaby()) limbSwing *= 3f;
			return limbSwing;
		});

		MolangQueries.<Object>setActorVariable("variable.limb_swing_amount", actor -> {
			if (!(actor.animatable() instanceof LivingEntity entity) || !canAnimateLimbs(entity)) return 0;
			return Math.min(entity.walkAnimation.speed(actor.partialTick()), 1f);
		});
	}

	private static boolean canAnimateLimbs(LivingEntity entity) {
		boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
		return !shouldSit && entity.isAlive();
	}

}
