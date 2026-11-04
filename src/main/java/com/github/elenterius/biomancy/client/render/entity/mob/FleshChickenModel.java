package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshChicken;

public class FleshChickenModel<T extends FleshChicken> extends DefaultedEntityGeoModel<T> {

	public FleshChickenModel() {
		super(BiomancyMod.rl("mob/flesh_chicken"));
	}

}
