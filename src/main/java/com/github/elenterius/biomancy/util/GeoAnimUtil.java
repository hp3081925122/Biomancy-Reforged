package com.github.elenterius.biomancy.util;

import com.geckolib.animation.AnimationController;

public final class GeoAnimUtil {

	private GeoAnimUtil() {}

	public static boolean isStopped(AnimationController<?> controller) {
		return controller.getCurrentRawAnimation() == null || controller.hasAnimationFinished();
	}

}
