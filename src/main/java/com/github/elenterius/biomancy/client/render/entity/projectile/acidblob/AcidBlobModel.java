package com.github.elenterius.biomancy.client.render.entity.projectile.acidblob;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.projectile.AcidBlobProjectile;

public class AcidBlobModel extends DefaultedEntityGeoModel<AcidBlobProjectile> {

	public AcidBlobModel() {
		super(BiomancyMod.rl("projectile/acid_blob"));
	}

}
