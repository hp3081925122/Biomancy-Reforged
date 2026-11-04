package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshPig;

public class FleshPigModel<T extends FleshPig> extends DefaultedEntityGeoModel<T> {

	public FleshPigModel() {
		super(BiomancyMod.rl("mob/flesh_pig"));
	}

}
