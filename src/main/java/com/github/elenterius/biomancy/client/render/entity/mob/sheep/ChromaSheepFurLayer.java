package com.github.elenterius.biomancy.client.render.entity.mob.sheep;

import com.github.elenterius.biomancy.init.client.ModRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;

public class ChromaSheepFurLayer extends RenderLayer<SheepRenderState, SheepModel> {

	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool.png");
	private static final Identifier BABY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool_baby.png");

	private final EntityModel<SheepRenderState> adultModel;
	private final EntityModel<SheepRenderState> babyModel;

	public ChromaSheepFurLayer(RenderLayerParent<SheepRenderState, SheepModel> renderer, EntityModelSet modelSet) {
		super(renderer);
		adultModel = new SheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_WOOL));
		babyModel = new SheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_BABY_WOOL));
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SheepRenderState state, float yRot, float xRot) {
		if (state.isSheared || state.isInvisible) return;

		EntityModel<SheepRenderState> model = state.isBaby ? babyModel : adultModel;
		Identifier texture = state.isBaby ? BABY_TEXTURE : TEXTURE;
		submitNodeCollector.submitModel(model, state, poseStack, ModRenderTypes.getCutoutPartyTime(texture), lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0f), -1, null, state.outlineColor, null);
	}

}
