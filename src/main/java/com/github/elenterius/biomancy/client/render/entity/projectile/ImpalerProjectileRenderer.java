package com.github.elenterius.biomancy.client.render.entity.projectile;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.projectile.ImpalerProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ImpalerProjectileRenderer extends OrientedGeoProjectileRenderer<ImpalerProjectile> {

	public ImpalerProjectileRenderer(EntityRendererProvider.Context context) {
		super(context, new DefaultedEntityGeoModel<>(BiomancyMod.rl("projectile/impaler_projectile")));
	}

}
