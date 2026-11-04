package com.github.elenterius.biomancy.init.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class ModArmPoses {

	public static final HumanoidModel.ArmPose HOLD_AND_AIM_GUN_TWO_HANDED = HumanoidModel.ArmPose.valueOf("BIOMANCY_HOLD_AND_AIM_GUN_TWO_HANDED");

	private ModArmPoses() {}

	static void init() {}

	public static final class Proxies {
		public static final EnumProxy<HumanoidModel.ArmPose> HOLD_AND_AIM_GUN_TWO_HANDED = new EnumProxy<>(HumanoidModel.ArmPose.class, true, true, (IArmPoseTransformer) (model, state, arm) -> {
			boolean rightHanded = arm == HumanoidArm.RIGHT;
			float elapsedTicks = Mth.clamp(state.ticksUsingItem, 0f, 5f);
			float progress = elapsedTicks / 2.5f;

			ModelPart mainHand = rightHanded ? model.rightArm : model.leftArm;
			ModelPart offHand = rightHanded ? model.leftArm : model.rightArm;

			mainHand.yRot = (rightHanded ? -0.3f : 0.3f) + model.head.yRot;
			mainHand.xRot = -Mth.lerp(progress, Mth.HALF_PI * 0.8f, Mth.HALF_PI) + model.head.xRot + 0.1f;

			offHand.yRot = Mth.lerp(progress, 0.774533f, 0.6f) * (rightHanded ? 1f : -1f) + model.head.yRot;
			offHand.xRot = -Mth.lerp(progress, 1.15017f, 1.5f) + model.head.xRot;
		});

		private Proxies() {}
	}

}
