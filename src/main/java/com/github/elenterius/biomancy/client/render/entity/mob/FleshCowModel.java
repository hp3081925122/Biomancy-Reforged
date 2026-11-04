package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshCow;

public class FleshCowModel<T extends FleshCow> extends DefaultedEntityGeoModel<T> {

	public FleshCowModel() {
		super(BiomancyMod.rl("mob/flesh_cow"));
	}

}
