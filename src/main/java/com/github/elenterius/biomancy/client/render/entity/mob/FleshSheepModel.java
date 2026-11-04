package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.mob.FleshSheep;

public class FleshSheepModel<T extends FleshSheep> extends DefaultedEntityGeoModel<T> {

	public FleshSheepModel() {
		super(BiomancyMod.rl("mob/flesh_sheep"));
	}

}
