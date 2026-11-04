package com.github.elenterius.biomancy.client.render.item.injector;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import com.geckolib.model.DefaultedItemGeoModel;

public class InjectorModel extends DefaultedItemGeoModel<InjectorItem> {

	public InjectorModel() {
		super(BiomancyMod.rl("injector"));
	}

}
