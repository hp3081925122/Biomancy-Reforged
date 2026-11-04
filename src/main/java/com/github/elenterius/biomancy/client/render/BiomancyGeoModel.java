package com.github.elenterius.biomancy.client.render;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class BiomancyGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

	private static final DataTicket<Identifier> MODEL = DataTicket.create(BiomancyMod.MOD_ID + ":model", Identifier.class);
	private static final DataTicket<Identifier> TEXTURE = DataTicket.create(BiomancyMod.MOD_ID + ":texture", Identifier.class);
	private static final DataTicket<RenderType> RENDER_TYPE = DataTicket.create(BiomancyMod.MOD_ID + ":render_type", RenderType.class);

	public abstract Identifier getModelResource(T animatable);

	public abstract Identifier getTextureResource(T animatable);

	public @Nullable RenderType getRenderType(T animatable, Identifier texture) {
		return null;
	}

	@Override
	public void addAdditionalStateData(T animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
		Identifier texture = getTextureResource(animatable);
		renderState.addGeckolibData(MODEL, getModelResource(animatable));
		renderState.addGeckolibData(TEXTURE, texture);
		RenderType renderType = getRenderType(animatable, texture);
		if (renderType != null) {
			renderState.addGeckolibData(RENDER_TYPE, renderType);
		}
	}

	@Override
	public final Identifier getModelResource(GeoRenderState renderState) {
		return renderState.getGeckolibData(MODEL);
	}

	@Override
	public final Identifier getTextureResource(GeoRenderState renderState) {
		return renderState.getGeckolibData(TEXTURE);
	}

	public static @Nullable RenderType getRenderType(GeoRenderState renderState) {
		return renderState.getGeckolibData(RENDER_TYPE);
	}

}
