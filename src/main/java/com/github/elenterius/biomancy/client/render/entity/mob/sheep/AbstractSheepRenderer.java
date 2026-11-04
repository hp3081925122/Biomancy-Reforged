package com.github.elenterius.biomancy.client.render.entity.mob.sheep;

import net.minecraft.client.model.animal.sheep.BabySheepModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.sheep.Sheep;

@SuppressWarnings("deprecation")
public abstract class AbstractSheepRenderer<T extends Sheep, S extends SheepRenderState> extends AgeableMobRenderer<T, S, SheepModel> {

	protected static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep.png");
	protected static final Identifier BABY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_baby.png");

	protected AbstractSheepRenderer(EntityRendererProvider.Context context) {
		super(context, new SheepModel(context.bakeLayer(ModelLayers.SHEEP)), new BabySheepModel(context.bakeLayer(ModelLayers.SHEEP_BABY)), 0.7f);
	}

	@Override
	public Identifier getTextureLocation(S state) {
		return state.isBaby ? BABY_TEXTURE : TEXTURE;
	}

	@Override
	public void extractRenderState(T sheep, S state, float partialTick) {
		super.extractRenderState(sheep, state, partialTick);
		state.headEatAngleScale = sheep.getHeadEatAngleScale(partialTick);
		state.headEatPositionScale = sheep.getHeadEatPositionScale(partialTick);
		state.isSheared = sheep.isSheared();
		state.woolColor = sheep.getColor();
	}

}
