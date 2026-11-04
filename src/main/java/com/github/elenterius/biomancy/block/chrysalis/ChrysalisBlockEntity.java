package com.github.elenterius.biomancy.block.chrysalis;

import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.HolderLookup;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ChrysalisBlockEntity extends BlockEntity implements Nameable {

	protected static final String CUSTOM_NAME_KEY = "CustomName";

	protected @Nullable Component name;
	private @Nullable CompoundTag entityTag;

	public ChrysalisBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CHRYSALIS.get(), pos, state);
	}

	public boolean isEmpty() {
		return entityTag == null;
	}

	@Override
	public Component getName() {
		return name != null ? name : ModBlocks.CHRYSALIS.get().getName();
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Override
	public @Nullable Component getCustomName() {
		return name;
	}

	public void setCustomName(@Nullable Component name) {
		this.name = name;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.storeNullable(CUSTOM_NAME_KEY, ComponentSerialization.CODEC, name);
		output.storeNullable(Chrysalis.ENTITY_KEY, CompoundTag.CODEC, entityTag);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		name = parseCustomNameSafe(input, CUSTOM_NAME_KEY);
		input.read(Chrysalis.ENTITY_KEY, CompoundTag.CODEC).ifPresent(tag -> entityTag = tag);
	}

}
