package com.github.elenterius.biomancy.api.nutrients;

import com.github.elenterius.biomancy.api.nutrients.fluid.FluidFuelConsumerHandler;
import com.github.elenterius.biomancy.inventory.Notify;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public class FuelHandlerImpl implements FuelHandler, ValueIOSerializable {
	private final int maxFuel;
	private final Predicate<ItemStack> fuelPredicate;
	private final ToIntFunction<ItemStack> fuelValueFunc;
	private final Notify changeNotifier;
	private int fuel;

	private final FluidFuelConsumerHandler fluidConsumer;

	public FuelHandlerImpl(int maxFuel, Predicate<ItemStack> fuelPredicate, ToIntFunction<ItemStack> fuelValueFunc, Notify changeNotifier) {
		this.maxFuel = maxFuel;
		this.fuelPredicate = fuelPredicate;
		this.fuelValueFunc = fuelValueFunc;
		this.changeNotifier = changeNotifier;

		fluidConsumer = new FluidFuelConsumerHandler(this);
	}

	public static FuelHandlerImpl createNutrientFuelHandler(int maxFuel, Notify changeNotifier) {
		return new FuelHandlerImpl(maxFuel, Nutrients::isValidFuel, Nutrients::getFuelValue, changeNotifier);
	}

	@Override
	public int getFuelAmount() {
		return fuel;
	}

	@Override
	public void setFuelAmount(int amount) {
		boolean flag = amount != fuel;
		fuel = amount;
		if (flag) setChanged();
	}

	@Override
	public int getMaxFuelAmount() {
		return maxFuel;
	}

	@Override
	public void addFuelAmount(int amount) {
		setFuelAmount(Mth.clamp(fuel + amount, 0, maxFuel));
	}

	@Override
	public boolean isValidFuel(ItemStack stack) {
		return fuelPredicate.test(stack);
	}

	@Override
	public int getFuelCost(int craftingCostNutrients) {
		return craftingCostNutrients;
	}

	@Override
	public int getFuelValue(ItemStack stack) {
		return fuelValueFunc.applyAsInt(stack);
	}

	public void setChanged() {
		changeNotifier.invoke();
	}

	@Override
	public void serialize(ValueOutput output) {
		output.putInt("Amount", fuel);
		fluidConsumer.serialize(output.child("FluidConsumer"));
	}

	@Override
	public void deserialize(ValueInput input) {
		fuel = input.getIntOr("Amount", 0);
		fluidConsumer.deserialize(input.childOrEmpty("FluidConsumer"));
	}

	public FluidFuelConsumerHandler getFluidConsumer() {
		return fluidConsumer;
	}

}
