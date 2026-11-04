package com.github.elenterius.biomancy.client.render.item.armor;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.armor.AcolyteArmorItem;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class AcolyteArmorRenderer extends GeoArmorRenderer<AcolyteArmorItem, HumanoidRenderState> {

	public AcolyteArmorRenderer() {
		super(new DefaultedItemGeoModel<>(BiomancyMod.rl("armor/alchemist_armor")));
	}

}
