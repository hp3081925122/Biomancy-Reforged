package com.github.elenterius.biomancy.client.extensions;

import com.github.elenterius.biomancy.init.client.ModArmPoses;
import com.github.elenterius.biomancy.item.weapon.gun.ImpalerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ImpalerItemExtensions implements IClientItemExtensions {

	private static boolean isHandPartOfArm(LocalPlayer player, HumanoidArm arm, InteractionHand hand) {
		InteractionHand handOfCurrentArm = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		return hand == handOfCurrentArm;
	}

	private static void applyArmTransform(PoseStack poseStack, HumanoidArm arm, float progress, float ticks) {
		float direction = arm == HumanoidArm.RIGHT ? 1f : -1f;
		float invProgress = 1f - progress;
		poseStack.mulPose(Axis.YP.rotationDegrees(10f * invProgress + direction * Mth.cos(ticks * 0.09f) * 1f));
		poseStack.mulPose(Axis.XP.rotationDegrees(-15f * invProgress + direction * Mth.sin(ticks * 0.067f) * 1f));
		poseStack.translate(0.56f * direction, -0.52f, -0.72f);
	}

	@Override
	public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
		return ModArmPoses.HOLD_AND_AIM_GUN_TWO_HANDED;
	}

	@Override
	public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
		if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && isHandPartOfArm(player, arm, player.getUsedItemHand())) {
			float elapsedDuration = (float) itemInHand.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - partialTick + 1f);
			float maxChargeDuration = itemInHand.getItem() instanceof ImpalerItem impaler ? impaler.getDelayBetweenShots(itemInHand) : 1f;
			float aimProgress = elapsedDuration / maxChargeDuration;
			if (aimProgress > 1f) {
				aimProgress = 1f;
			}

			applyArmTransform(poseStack, arm, aimProgress, player.tickCount + partialTick);
			return true;
		}

		if (!player.isAutoSpinAttack() && !player.swinging) {
			applyArmTransform(poseStack, arm, 0f, player.tickCount + partialTick);
			return true;
		}

		return false;
	}

}
