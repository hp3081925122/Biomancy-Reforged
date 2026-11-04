package com.github.elenterius.biomancy.client.render.block.digester;

import com.github.elenterius.biomancy.block.digester.DigesterBlockEntity;
import com.github.elenterius.biomancy.client.render.block.CustomGeoBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DigesterRenderer extends CustomGeoBlockRenderer<DigesterBlockEntity> {

	public DigesterRenderer(BlockEntityRendererProvider.Context context) {
		super(context, new DigesterModel(), RenderTypes::entityCutoutCull);
	}

}
