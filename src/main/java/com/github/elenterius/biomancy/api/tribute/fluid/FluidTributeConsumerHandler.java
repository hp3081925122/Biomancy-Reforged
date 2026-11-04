package com.github.elenterius.biomancy.api.tribute.fluid;

import com.github.elenterius.biomancy.api.tribute.SacrificeHandler;
import com.github.elenterius.biomancy.inventory.Notify;
import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.stream.LongStream;

public class FluidTributeConsumerHandler implements ResourceHandler<FluidResource>, ValueIOSerializable {

	public static final String MILLI_TRIBUTE_BUFFER_KEY = "MilliTributeBuffer";

	private final SacrificeHandler sacrificeHandler;
	private final Notify onChange;
	private final long[] milliTributeBuffer = new long[6];

	private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
		@Override
		protected Snapshot createSnapshot() {
			return new Snapshot(sacrificeHandler.captureState(), milliTributeBuffer.clone());
		}

		@Override
		protected void revertToSnapshot(Snapshot snapshot) {
			sacrificeHandler.restoreState(snapshot.state());
			System.arraycopy(snapshot.buffer(), 0, milliTributeBuffer, 0, milliTributeBuffer.length);
		}

		@Override
		protected void onRootCommit(Snapshot originalState) {
			if (sacrificeHandler.isDirty()) {
				onChange.invoke();
				sacrificeHandler.setDirty(false);
			}
		}
	};

	public FluidTributeConsumerHandler(SacrificeHandler sacrificeHandler, Notify onChange) {
		this.sacrificeHandler = sacrificeHandler;
		this.onChange = onChange;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.EMPTY;
	}

	@Override
	public long getAmountAsLong(int index) {
		return 0;
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return 250;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return !resource.isEmpty() && !sacrificeHandler.isFull() && FluidTributes.isValid(resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		if (amount == 0) return 0;
		if (sacrificeHandler.isFull()) return 0;

		FluidStack stack = resource.toStack(amount);
		FluidToTributeConversion conversion = FluidTributes.getConversion(stack);
		if (conversion == null) return 0;

		FluidTribute fluidTribute = conversion.getTributePerUnit(stack);
		if (fluidTribute.isEmpty()) return 0;
		if (fluidTribute.biomass() > 0 && sacrificeHandler.isBiomassFull()) return 0;

		long amountToConsume = amount;

		journal.updateSnapshots(transaction);

		int biomassYield = (int) ((milliTributeBuffer[0] + fluidTribute.biomass() * amountToConsume) / FluidTribute.UNIT_SCALE);
		int biomassToFill = Math.min(sacrificeHandler.getMaxBiomass() - sacrificeHandler.getBiomassAmount(), biomassYield);

		if (biomassToFill > 0) {
			long biomassMissing = biomassToFill * FluidTribute.UNIT_SCALE - milliTributeBuffer[0];
			amountToConsume = Mth.ceil((float) biomassMissing / fluidTribute.biomass());

			sacrificeHandler.addBiomass(biomassToFill);

			long remainder = (fluidTribute.biomass() * amountToConsume - biomassMissing);
			milliTributeBuffer[0] = remainder;
		}

		int yield;

		milliTributeBuffer[1] += fluidTribute.lifeEnergy() * amountToConsume;
		yield = (int) (milliTributeBuffer[1] / FluidTribute.UNIT_SCALE);
		sacrificeHandler.addLifeEnergy(yield);
		milliTributeBuffer[1] -= yield * FluidTribute.UNIT_SCALE;

		milliTributeBuffer[2] += fluidTribute.successModifier() * amountToConsume;
		yield = (int) (milliTributeBuffer[2] / FluidTribute.UNIT_SCALE);
		sacrificeHandler.addSuccess(yield);
		milliTributeBuffer[2] -= yield * FluidTribute.UNIT_SCALE;

		milliTributeBuffer[3] += fluidTribute.hostileModifier() * amountToConsume;
		yield = (int) (milliTributeBuffer[3] / FluidTribute.UNIT_SCALE);
		sacrificeHandler.addHostile(yield);
		milliTributeBuffer[3] -= yield * FluidTribute.UNIT_SCALE;

		milliTributeBuffer[4] += fluidTribute.diseaseModifier() * amountToConsume;
		yield = (int) (milliTributeBuffer[4] / FluidTribute.UNIT_SCALE);
		sacrificeHandler.addDisease(yield);
		milliTributeBuffer[4] -= yield * FluidTribute.UNIT_SCALE;

		milliTributeBuffer[5] += fluidTribute.anomalyModifier() * amountToConsume;
		yield = (int) (milliTributeBuffer[5] / FluidTribute.UNIT_SCALE);
		sacrificeHandler.addAnomaly(yield);
		milliTributeBuffer[5] -= yield * FluidTribute.UNIT_SCALE;

		return (int) amountToConsume;
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public void serialize(ValueOutput output) {
		output.store(MILLI_TRIBUTE_BUFFER_KEY, Codec.LONG_STREAM, LongStream.of(milliTributeBuffer));
	}

	@Override
	public void deserialize(ValueInput input) {
		input.read(MILLI_TRIBUTE_BUFFER_KEY, Codec.LONG_STREAM).ifPresent(stream -> {
			long[] buffer = stream.toArray();
			System.arraycopy(buffer, 0, milliTributeBuffer, 0, Math.min(buffer.length, milliTributeBuffer.length));
		});
	}

	private record Snapshot(int[] state, long[] buffer) {}

}
