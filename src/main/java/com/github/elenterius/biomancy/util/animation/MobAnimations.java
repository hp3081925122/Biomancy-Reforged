package com.github.elenterius.biomancy.util.animation;

import com.geckolib.animatable.GeoEntity;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

public final class MobAnimations {

	private MobAnimations() {}

	public static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenPlay("walk");
	public static final RawAnimation BABY_TRANSFORM_ANIMATION = RawAnimation.begin().thenPlay("baby_transform");

	public static <T extends GeoEntity> AnimationController<T> walkController(T geoEntity) {
		return new AnimationController<>("walk", state -> {
			if (state.isMoving()) {
				return state.setAndContinue(WALK_ANIMATION);
			}
			return PlayState.STOP;
		});
	}

	public static <T extends GeoEntity> AnimationController<T> babyTransformController(T geoEntity) {
		return new AnimationController<>("baby_transform", state -> {
			if (state.animatable() instanceof LivingEntity livingEntity && livingEntity.isBaby()) {
				return state.setAndContinue(BABY_TRANSFORM_ANIMATION);
			}
			return PlayState.STOP;
		});
	}

}
