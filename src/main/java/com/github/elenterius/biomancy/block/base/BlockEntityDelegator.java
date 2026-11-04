package com.github.elenterius.biomancy.block.base;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
	protected void saveForSyncToClient(ValueOutput output) {}

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
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		isValid = false;
		input.getLong("DelegatePos").ifPresent(pos -> {
			delegatePos = BlockPos.of(pos);
			isValid = true;
		});
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (isValid) {
			output.putLong("DelegatePos", delegatePos.asLong());
		}
	}

	public <T, C> @Nullable T getDelegateCapability(BlockCapability<T, C> cap, C context) {
		if (level == null || remove) return null;
		BlockEntity delegate = getDelegate();
		if (delegate == null || delegate.isRemoved()) return null;
		return level.getCapability(cap, delegate.getBlockPos(), delegate.getBlockState(), delegate, context);
	}

}
