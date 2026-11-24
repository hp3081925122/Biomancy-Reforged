package com.github.elenterius.biomancy.client.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

// 精华提取器客户端逻辑钩子，避免通用物品类直接引用 Minecraft
public final class ExtractorClientHooks {

	private ExtractorClientHooks() {}

	public static boolean isCrosshairOnLivingEntity() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft != null && minecraft.crosshairPickEntity instanceof LivingEntity;
	}

}
