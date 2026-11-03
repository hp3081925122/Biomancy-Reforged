package com.github.elenterius.biomancy.client.util;

import software.bernie.geckolib.loading.math.MathParser;

import java.util.function.DoubleSupplier;

public final class MolangUtil {

	private MolangUtil() {}

	public static void setMemoizedValue(String name, DoubleSupplier supplier) {
		MathParser.setVariable(name, new DoubleSupplier() {
			private boolean computed = false;
			private double value;

			@Override
			public double getAsDouble() {
				if (!computed) {
					value = supplier.getAsDouble();
					computed = true;
				}
				return value;
			}
		});
	}

}
