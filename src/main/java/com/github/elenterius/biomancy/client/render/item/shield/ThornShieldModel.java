package com.github.elenterius.biomancy.client.render.item.shield;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.shield.ThornShieldItem;
import com.geckolib.model.DefaultedItemGeoModel;

public class ThornShieldModel extends DefaultedItemGeoModel<ThornShieldItem> {

	public ThornShieldModel() {
		super(BiomancyMod.rl("shield/thorn_shield"));
	}

}
