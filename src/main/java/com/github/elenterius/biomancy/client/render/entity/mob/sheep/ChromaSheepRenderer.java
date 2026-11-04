package com.github.elenterius.biomancy.client.render.entity.mob.sheep;

import com.github.elenterius.biomancy.entity.mob.ChromaSheep;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SheepRenderState;

public class ChromaSheepRenderer extends AbstractSheepRenderer<ChromaSheep, SheepRenderState> {

	public ChromaSheepRenderer(EntityRendererProvider.Context context) {
		super(context);
		addLayer(new ChromaSheepFurLayer(this, context.getModelSet()));
	}

	@Override
	public SheepRenderState createRenderState() {
		return new SheepRenderState();
	}

}
