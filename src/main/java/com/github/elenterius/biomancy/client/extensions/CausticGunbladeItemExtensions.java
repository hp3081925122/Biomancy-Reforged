package com.github.elenterius.biomancy.client.extensions;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jspecify.annotations.Nullable;

public class CausticGunbladeItemExtensions implements IClientItemExtensions {

	@Override
	public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
		return entityLiving.isUsingItem() ? HumanoidModel.ArmPose.CROSSBOW_HOLD : null;
	}

}
