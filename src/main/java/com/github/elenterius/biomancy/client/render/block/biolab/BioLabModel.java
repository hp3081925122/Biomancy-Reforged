package com.github.elenterius.biomancy.client.render.block.biolab;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.biolab.BioLabBlockEntity;

public class BioLabModel extends DefaultedBlockGeoModel<BioLabBlockEntity> {

	public BioLabModel() {
		super(BiomancyMod.rl("bio_lab"));
	}

}
