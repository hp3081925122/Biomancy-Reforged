package com.github.elenterius.biomancy.crafting.state;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public enum CraftingState {
	NONE(0), IN_PROGRESS(1), CANCELED(2), COMPLETED(3);

	public static final String NBT_KEY = "CraftingState";
	final byte id;

	CraftingState(int id) {
		this.id = (byte) id;
	}

	public static CraftingState fromId(byte id) {
		return switch (id) {
			case 1 -> IN_PROGRESS;
			case 2 -> CANCELED;
			case 3 -> COMPLETED;
			default -> NONE;
		};
	}

	public static void toNBT(ValueOutput output, CraftingState state) {
		output.putByte(NBT_KEY, state.id);
	}

	public static CraftingState fromNBT(ValueInput input) {
		return CraftingState.fromId(input.getByteOr(NBT_KEY, (byte) 0));
	}

	public boolean isCanceled() {
		return this == CANCELED;
	}

}
