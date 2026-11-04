package com.github.elenterius.biomancy.api.nutrients.fluid;

import com.github.elenterius.biomancy.api.nutrients.FuelHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class FluidFuelConsumerHandler implements ResourceHandler<FluidResource>, ValueIOSerializable {

	public static final String MILLI_FUEL_BUFFER_KEY = "MilliFuelBuffer";

	private final FuelHandler fuelHandler;
	private long milliFuelBuffer;

	private final SnapshotJournal<long[]> journal = new SnapshotJournal<>() {
		@Override
		protected long[] createSnapshot() {
			return new long[]{fuelHandler.getFuelAmount(), milliFuelBuffer};
		}

		@Override
		protected void revertToSnapshot(long[] snapshot) {
			fuelHandler.setFuelAmount((int) snapshot[0]);
			milliFuelBuffer = snapshot[1];
		}
	};

	public FluidFuelConsumerHandler(FuelHandler fuelHandler) {
		this.fuelHandler = fuelHandler;
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
		return fuelHandler.getMaxFuelAmount();
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return !resource.isEmpty() && FluidNutrients.isValid(resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		if (amount == 0) return 0;
		if (fuelHandler.getFuelAmount() >= fuelHandler.getMaxFuelAmount()) return 0;

		FluidStack stack = resource.toStack(amount);
		FluidToFuelConversion conversion = FluidNutrients.getConversion(stack);
		if (conversion == null) return 0;

		int milliFuel = conversion.getMilliFuelPerUnit(stack);
		if (milliFuel <= 0) return 0;

		long amountToConsume = amount;
		int fuelYield = (int) ((milliFuelBuffer + milliFuel * amountToConsume) / FluidToFuelConversion.MILLI_FUEL_SCALE);

		if (fuelYield > 0) {
			int fuelToFill = Math.min(fuelHandler.getMaxFuelAmount() - fuelHandler.getFuelAmount(), fuelYield);
			long fuelMissing = (long) fuelToFill * FluidToFuelConversion.MILLI_FUEL_SCALE - milliFuelBuffer;

			amountToConsume = (int) (fuelMissing / milliFuel);

			journal.updateSnapshots(transaction);
			fuelHandler.addFuelAmount(fuelToFill);
			milliFuelBuffer = milliFuel * amountToConsume - fuelMissing;
		}
		else {
			journal.updateSnapshots(transaction);
			milliFuelBuffer += milliFuel * amountToConsume;
		}

		return (int) amountToConsume;
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public void serialize(ValueOutput output) {
		output.putLong(MILLI_FUEL_BUFFER_KEY, milliFuelBuffer);
	}

	@Override
	public void deserialize(ValueInput input) {
		milliFuelBuffer = input.getLongOr(MILLI_FUEL_BUFFER_KEY, 0L);
	}

}
