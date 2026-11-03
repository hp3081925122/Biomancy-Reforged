package com.github.elenterius.biomancy.block.base;

import com.github.elenterius.biomancy.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

@Deprecated
public class BlockEntityDelegator extends SimpleSyncedBlockEntity implements IBlockEntityDelegator {

	private BlockPos delegatePos = BlockPos.ZERO;
	private boolean isValid = false;

	public BlockEntityDelegator(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BE_DELEGATOR.get(), pos, state);
	}

	@Override
	protected void saveForSyncToClient(CompoundTag tag, HolderLookup.Provider registries) {}

	@Override
	public BlockPos getDelegatePos() {
		return delegatePos;
	}

	@Override
	public @Nullable BlockEntity getDelegate() {
		if (level != null && isValid && !remove) {
			BlockEntity blockEntity = level.getBlockEntity(delegatePos);
			if (blockEntity != null && (blockEntity == this || blockEntity.isRemoved())) {
				setDelegate(null);
				return null;
			}
			if (blockEntity == null) {
				isValid = false;
			}
			return blockEntity;
		}
		return null;
	}

	@Override
	public void setDelegate(@Nullable BlockEntity blockEntity) {
		if (blockEntity == this || blockEntity == null) {
			delegatePos = BlockPos.ZERO;
			isValid = false;
		}
		if (blockEntity != null) {
			delegatePos = blockEntity.getBlockPos();
			isValid = true;
		}
		invalidateCapabilities();
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		isValid = false;
		if (tag.contains("DelegatePos")) {
			delegatePos = BlockPos.of(tag.getLong("DelegatePos"));
			isValid = true;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (isValid) {
			tag.putLong("DelegatePos", delegatePos.asLong());
		}
	}

	public <T, C> @Nullable T getDelegateCapability(BlockCapability<T, C> cap, C context) {
		if (level == null || remove) return null;
		BlockEntity delegate = getDelegate();
		if (delegate == null || delegate.isRemoved()) return null;
		return level.getCapability(cap, delegate.getBlockPos(), delegate.getBlockState(), delegate, context);
	}

}
