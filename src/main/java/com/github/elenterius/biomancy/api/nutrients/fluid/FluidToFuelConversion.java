package com.github.elenterius.biomancy.api.nutrients.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface FluidToFuelConversion {

	int MILLI_FUEL_SCALE = 1000;

	FluidToFuelConversion IDENTITY = resource -> MILLI_FUEL_SCALE;

	int getMilliFuelPerUnit(FluidStack resource);

}
