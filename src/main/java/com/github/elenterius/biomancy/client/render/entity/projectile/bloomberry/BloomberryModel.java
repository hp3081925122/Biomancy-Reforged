package com.github.elenterius.biomancy.client.render.entity.projectile.bloomberry;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.projectile.BloomberryProjectile;

public class BloomberryModel extends DefaultedEntityGeoModel<BloomberryProjectile> {

	public BloomberryModel() {
		super(BiomancyMod.rl("projectile/bloomberry"));
	}

}
