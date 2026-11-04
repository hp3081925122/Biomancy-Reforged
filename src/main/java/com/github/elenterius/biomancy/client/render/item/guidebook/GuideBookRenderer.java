package com.github.elenterius.biomancy.client.render.item.guidebook;

import com.github.elenterius.biomancy.client.render.item.BiomancyGeoItemRenderer;
import com.github.elenterius.biomancy.item.GuideBookItem;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class GuideBookRenderer extends BiomancyGeoItemRenderer<GuideBookItem> {

	public GuideBookRenderer() {
		super(new GuideBookModel(), RenderTypes::entityCutoutCull);
	}

}
