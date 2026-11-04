package com.github.elenterius.biomancy.client.render.block.tongue;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.tongue.TongueBlockEntity;

public class TongueModel extends DefaultedBlockGeoModel<TongueBlockEntity> {

	public TongueModel() {
		super(BiomancyMod.rl("tongue"));
	}

}
