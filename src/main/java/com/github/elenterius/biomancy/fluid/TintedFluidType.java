package com.github.elenterius.biomancy.fluid;

import net.neoforged.neoforge.fluids.FluidType;

public class TintedFluidType extends FluidType {

	protected final int colorARGB;

	public TintedFluidType(Properties properties, int colorARGB) {
		super(properties);
		this.colorARGB = colorARGB;
	}

	public int getTintColor() {
		return colorARGB;
	}

}
