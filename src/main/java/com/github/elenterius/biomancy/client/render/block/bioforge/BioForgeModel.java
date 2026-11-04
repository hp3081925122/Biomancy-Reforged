package com.github.elenterius.biomancy.client.render.block.bioforge;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.bioforge.BioForgeBlockEntity;

public class BioForgeModel extends DefaultedBlockGeoModel<BioForgeBlockEntity> {

	public BioForgeModel() {
		super(BiomancyMod.rl("bio_forge"));
	}

}
