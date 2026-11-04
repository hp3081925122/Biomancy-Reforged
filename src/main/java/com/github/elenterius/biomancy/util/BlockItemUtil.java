package com.github.elenterius.biomancy.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockItemUtil {

	private BlockItemUtil() {}

	public static @Nullable CompoundTag getBlockEntityData(ItemStack stack) {
		TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		return data != null ? data.copyTagWithoutId() : null;
	}

	public static void saveToItem(BlockEntity blockEntity, ItemStack stack, HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		blockEntity.saveCustomOnly(output);
		blockEntity.removeComponentsFromTag(output);
		BlockItem.setBlockEntityData(stack, blockEntity.getType(), output);
		stack.applyComponents(blockEntity.collectComponents());
	}

	public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, CompoundTag tag) {
		CompoundTag copy = tag.copy();
		copy.remove("id");
		if (copy.isEmpty()) {
			stack.remove(DataComponents.BLOCK_ENTITY_DATA);
		}
		else {
			stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(type, copy));
		}
	}

}
