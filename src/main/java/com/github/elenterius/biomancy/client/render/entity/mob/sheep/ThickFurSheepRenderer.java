package com.github.elenterius.biomancy.client.render.entity.mob.sheep;

import com.github.elenterius.biomancy.entity.mob.ThickFurSheep;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ThickFurSheepRenderer extends AbstractSheepRenderer<ThickFurSheep, ThickFurSheepRenderState> {

	public ThickFurSheepRenderer(EntityRendererProvider.Context context) {
		super(context);
		addLayer(new ThickFurSheepLayer(this, context.getModelSet()));
	}

	@Override
	public ThickFurSheepRenderState createRenderState() {
		return new ThickFurSheepRenderState();
	}

	@Override
	public void extractRenderState(ThickFurSheep sheep, ThickFurSheepRenderState state, float partialTick) {
		super.extractRenderState(sheep, state, partialTick);
		state.woolSize = sheep.getWoolSize();
	}

}
