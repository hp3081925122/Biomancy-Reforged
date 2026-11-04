package com.github.elenterius.biomancy.client.render.item.impaler;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.weapon.gun.ImpalerItem;
import com.geckolib.model.DefaultedItemGeoModel;

public class ImpalerModel extends DefaultedItemGeoModel<ImpalerItem> {

	public ImpalerModel() {
		super(BiomancyMod.rl("weapon/impaler"));
	}

}
