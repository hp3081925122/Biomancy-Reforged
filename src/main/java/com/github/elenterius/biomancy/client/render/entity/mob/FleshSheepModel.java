package com.github.elenterius.biomancy.client.render.entity.mob;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshSheep;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import com.github.elenterius.biomancy.client.util.MolangUtil;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class FleshSheepModel<T extends FleshSheep> extends DefaultedEntityGeoModel<T> {

	public FleshSheepModel() {
		super(BiomancyMod.rl("mob/flesh_sheep"), true);
	}

	@Override
	public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {

		if (FleshSheep.Animations.getEatAnimationTick(animatable) > 0) {
			return;
		}

		GeoBone head = getAnimationProcessor().getBone("head");
		if (head == null) return;

		EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
		head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
		head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
	}

	@Override
	public void applyMolangQueries(AnimationState<T> animationState, double animTime) {
		super.applyMolangQueries(animationState, animTime);

		T animatable = animationState.getAnimatable();
		float partialTick = animationState.getPartialTick();

		MolangUtil.setMemoizedValue("variable.limb_swing", () -> {
			boolean shouldSit = animatable.isPassenger() && (animatable.getVehicle() != null && animatable.getVehicle().shouldRiderSit());

			float limbSwing = 0;

			if (!shouldSit && animatable.isAlive()) {
				limbSwing = animatable.walkAnimation.position(partialTick);
				if (animatable.isBaby()) limbSwing *= 3f;
			}

			return limbSwing;
		});

		MolangUtil.setMemoizedValue("variable.limb_swing_amount", () -> {
			boolean shouldSit = animatable.isPassenger() && (animatable.getVehicle() != null && animatable.getVehicle().shouldRiderSit());

			float limbSwingAmount = 0;

			if (!shouldSit && animatable.isAlive()) {
				limbSwingAmount = animatable.walkAnimation.speed(partialTick);
				if (limbSwingAmount > 1f) limbSwingAmount = 1f;
			}

			return limbSwingAmount;
		});
	}

}
