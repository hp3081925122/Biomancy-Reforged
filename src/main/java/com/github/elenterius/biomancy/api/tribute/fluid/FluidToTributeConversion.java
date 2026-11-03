package com.github.elenterius.biomancy.api.tribute.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface FluidToTributeConversion {

	FluidTribute getTributePerUnit(FluidStack resource);

}
