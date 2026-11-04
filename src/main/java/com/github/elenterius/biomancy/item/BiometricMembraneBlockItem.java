package com.github.elenterius.biomancy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.core.UUIDUtil;
import com.github.elenterius.biomancy.util.BlockItemUtil;
import com.github.elenterius.biomancy.block.membrane.BiometricMembraneBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BiometricMembraneBlockItem extends SimpleBlockItem {

	public BiometricMembraneBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		CompoundTag compoundTag = BlockItemUtil.getBlockEntityData(stack);
		if (compoundTag == null || !compoundTag.contains(BiometricMembraneBlockEntity.MEMBRANE_KEY)) {
			return super.getName(stack);
		}

		CompoundTag tag = compoundTag.getCompoundOrEmpty(BiometricMembraneBlockEntity.MEMBRANE_KEY);

		String inverted = tag.getBooleanOr(BiometricMembraneBlockEntity.IS_INVERTED_KEY, false) ? ".inverted" : "";
		String unique = tag.read(BiometricMembraneBlockEntity.ENTITY_UUID_KEY, UUIDUtil.CODEC).isPresent() ? ".unique" : "";

		return Component.translatable(getDescriptionId() + inverted + unique);
	}

}
