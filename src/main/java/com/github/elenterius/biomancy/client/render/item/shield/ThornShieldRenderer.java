package com.github.elenterius.biomancy.client.render.item.shield;

import com.github.elenterius.biomancy.client.render.item.BiomancyGeoItemRenderer;
import com.github.elenterius.biomancy.item.shield.ThornShieldItem;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ThornShieldRenderer extends BiomancyGeoItemRenderer<ThornShieldItem> {

	public ThornShieldRenderer() {
		super(new ThornShieldModel(), RenderTypes::entityCutoutCull);
	}

}
