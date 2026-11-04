package com.github.elenterius.biomancy.client.render.block.digester;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.digester.DigesterBlockEntity;

public class DigesterModel extends DefaultedBlockGeoModel<DigesterBlockEntity> {

	public DigesterModel() {
		super(BiomancyMod.rl("digester"));
	}

}
