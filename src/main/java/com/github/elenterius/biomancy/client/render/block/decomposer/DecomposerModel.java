package com.github.elenterius.biomancy.client.render.block.decomposer;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.decomposer.DecomposerBlockEntity;

public class DecomposerModel extends DefaultedBlockGeoModel<DecomposerBlockEntity> {

	public DecomposerModel() {
		super(BiomancyMod.rl("decomposer"));
	}

}
