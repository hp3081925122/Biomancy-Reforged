package com.github.elenterius.biomancy.client.render.item.guidebook;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.GuideBookItem;
import com.geckolib.model.DefaultedItemGeoModel;

public class GuideBookModel extends DefaultedItemGeoModel<GuideBookItem> {

	public GuideBookModel() {
		super(BiomancyMod.rl("guide_book"));
	}

}
