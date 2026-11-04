package com.github.elenterius.biomancy.client.render.entity.projectile.bloomberry;

import com.github.elenterius.biomancy.client.render.entity.projectile.TranslucentGeoProjectileRenderer;
import com.github.elenterius.biomancy.entity.projectile.BloomberryProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BloomberryProjectileRenderer extends TranslucentGeoProjectileRenderer<BloomberryProjectile> {

	public BloomberryProjectileRenderer(EntityRendererProvider.Context context) {
		super(context, new BloomberryModel());
	}

}
