package com.github.elenterius.biomancy.util.debug;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class DebugInfo {

	private DebugInfo() {}

	public interface EntityContext {
		void emitDebugGizmos(double cameraDistanceSqr, float partialTicks);
	}

	public interface ItemContext {
		void emitDebugGizmos(LivingEntity livingEntity, ItemStack mainHandItemStack, float partialTicks);
	}

}
