package com.github.elenterius.biomancy.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

public final class BlockItemUtil {

	private BlockItemUtil() {}

	public static @Nullable CompoundTag getBlockEntityData(ItemStack stack) {
		CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		return data != null ? data.copyTag() : null;
	}

}
