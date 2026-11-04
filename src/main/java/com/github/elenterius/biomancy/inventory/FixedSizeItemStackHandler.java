package com.github.elenterius.biomancy.inventory;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FixedSizeItemStackHandler extends ItemStackHandler implements SerializableItemHandler {

	public FixedSizeItemStackHandler(int size) {
		super(size);
	}

	@Override
	public void serialize(ValueOutput output) {
		ContainerHelper.saveAllItems(output, stacks);
	}

	@Override
	public void deserialize(ValueInput input) {
		setSize(stacks.size());
		ContainerHelper.loadAllItems(input, stacks);
		onLoad();
	}

}
