package com.github.elenterius.biomancy.entity.mob.fleshblob;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

class FleshBlobAttackGoal extends MeleeAttackGoal {

	public FleshBlobAttackGoal(FleshBlob mob, double speed) {
		super(mob, speed, true);
	}

	private double getSizePct() {
		return (double) (((FleshBlob) mob).getBlobSize() - FleshBlob.MIN_SIZE) / (double) (FleshBlob.MAX_SIZE - FleshBlob.MIN_SIZE);
	}

	@Override
	protected boolean canPerformAttack(LivingEntity attackTarget) {
		return isTimeToAttack() && mob.distanceToSqr(attackTarget) <= getAttackReachSqr(attackTarget);
	}

	private double getAttackReachSqr(LivingEntity attackTarget) {
		double actualAttackReach = Mth.lerp(getSizePct(), 0.25d, 0.75d);
		double inflatedHalfWidth = (mob.getBbWidth() + attackTarget.getBbWidth()) * 0.5d;
		double radius = inflatedHalfWidth + actualAttackReach;
		return radius * radius + 1e-7d;
	}

}
