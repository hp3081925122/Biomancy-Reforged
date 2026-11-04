package com.github.elenterius.biomancy.client.render.entity.projectile.acidblob;

import com.github.elenterius.biomancy.client.render.entity.projectile.TranslucentGeoProjectileRenderer;
import com.github.elenterius.biomancy.entity.projectile.AcidBlobProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AcidBlobProjectileRenderer extends TranslucentGeoProjectileRenderer<AcidBlobProjectile> {

	public AcidBlobProjectileRenderer(EntityRendererProvider.Context context) {
		super(context, new AcidBlobModel());
	}

}
