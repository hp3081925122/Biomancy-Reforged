package com.github.elenterius.biomancy.client.render.block.fleshkinchest;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.fleshkinchest.FleshkinChestBlockEntity;

public class FleshkinChestModel extends DefaultedBlockGeoModel<FleshkinChestBlockEntity> {

	public FleshkinChestModel() {
		super(BiomancyMod.rl("fleshkin_chest"));
	}

}
