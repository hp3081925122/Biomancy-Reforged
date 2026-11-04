package com.github.elenterius.biomancy.client.render.item;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.geckolibextras.GLibExtras;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class BiomancyGeoItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {

	private final Function<Identifier, RenderType> renderType;

	public BiomancyGeoItemRenderer(GeoModel<T> model) {
		this(model, RenderTypes::entityCutout);
	}

	public BiomancyGeoItemRenderer(GeoModel<T> model, Function<Identifier, RenderType> renderType) {
		super(model);
		this.renderType = renderType;
	}

	@Override
	public void addRenderData(T animatable, GeoItemRenderer.@Nullable RenderData renderData, GeoRenderState renderState, float partialTick) {
		if (renderData == null) return;

		renderState.addGeckolibData(ModDataTickets.ITEMSTACK, renderData.itemStack());

		Object host = getHost(renderData.itemOwner());
		if (host != null) {
			renderState.addGeckolibData(GLibExtras.ITEM_HOST_TICKET, host);
		}
	}

	private static @Nullable Object getHost(@Nullable ItemOwner owner) {
		while (owner instanceof ItemOwner.OffsetFromOwner offset) {
			owner = offset.owner();
		}
		return owner;
	}

	@Override
	public @Nullable RenderType getRenderType(GeoRenderState renderState, Identifier texture) {
		return renderType.apply(texture);
	}

}
