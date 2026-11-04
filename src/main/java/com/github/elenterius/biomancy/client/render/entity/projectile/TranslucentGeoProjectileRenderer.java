package com.github.elenterius.biomancy.client.render.entity.projectile;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class TranslucentGeoProjectileRenderer<T extends Entity & GeoAnimatable> extends OrientedGeoProjectileRenderer<T> {

	public TranslucentGeoProjectileRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
		super(context, model);
	}

	@Override
	public @Nullable RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
		if (!renderState.isInvisible) return RenderTypes.entityTranslucent(texture);
		return super.getRenderType(renderState, texture);
	}

}
