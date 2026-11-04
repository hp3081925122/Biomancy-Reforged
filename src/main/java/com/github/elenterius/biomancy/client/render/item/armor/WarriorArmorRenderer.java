package com.github.elenterius.biomancy.client.render.item.armor;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.armor.WarriorArmorItem;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class WarriorArmorRenderer extends GeoArmorRenderer<WarriorArmorItem, HumanoidRenderState> {

	public WarriorArmorRenderer() {
		super(new DefaultedItemGeoModel<>(BiomancyMod.rl("armor/warrior_armor")));
	}

}
