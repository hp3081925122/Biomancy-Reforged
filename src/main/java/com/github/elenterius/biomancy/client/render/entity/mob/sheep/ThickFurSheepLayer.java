package com.github.elenterius.biomancy.client.render.entity.mob.sheep;

import com.github.elenterius.biomancy.entity.mob.ThickFurSheep;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.color.ColorLerper;

public class ThickFurSheepLayer extends RenderLayer<ThickFurSheepRenderState, SheepModel> {

	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool.png");
	private static final Identifier BABY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool_baby.png");

	private final SheepFurModel limbsModel;
	private final SheepFurModel bodyModel;
	private final SheepFurModel babyLimbsModel;
	private final SheepFurModel babyBodyModel;

	public ThickFurSheepLayer(RenderLayerParent<ThickFurSheepRenderState, SheepModel> renderer, EntityModelSet modelSet) {
		super(renderer);
		limbsModel = createModel(modelSet, ModelLayers.SHEEP_WOOL, false);
		bodyModel = createModel(modelSet, ModelLayers.SHEEP_WOOL, true);
		babyLimbsModel = createModel(modelSet, ModelLayers.SHEEP_BABY_WOOL, false);
		babyBodyModel = createModel(modelSet, ModelLayers.SHEEP_BABY_WOOL, true);
	}

	private static SheepFurModel createModel(EntityModelSet modelSet, ModelLayerLocation layer, boolean bodyOnly) {
		SheepFurModel model = new SheepFurModel(modelSet.bakeLayer(layer));
		model.root().getChild("body").visible = bodyOnly;
		model.root().getChild("head").visible = !bodyOnly;
		model.root().getChild("left_front_leg").visible = !bodyOnly;
		model.root().getChild("right_front_leg").visible = !bodyOnly;
		model.root().getChild("left_hind_leg").visible = !bodyOnly;
		model.root().getChild("right_hind_leg").visible = !bodyOnly;
		return model;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ThickFurSheepRenderState state, float yRot, float xRot) {
		if (state.isSheared || state.isInvisible) return;

		int color = ColorLerper.Type.SHEEP.getColor(state.woolColor);
		Identifier texture = state.isBaby ? BABY_TEXTURE : TEXTURE;

		coloredCutoutModelCopyLayerRender(state.isBaby ? babyLimbsModel : limbsModel, texture, poseStack, submitNodeCollector, lightCoords, state, color, 0);

		poseStack.pushPose();
		float scale = 1f + (state.woolSize - 1f) / (float) (ThickFurSheep.MAX_WOOL_SIZE - 1);
		poseStack.scale(scale, scale, 0.5f + scale * 0.5f);
		poseStack.translate(0d, -(scale / 10d), scale * (scale * 0.6d) / 10d);
		coloredCutoutModelCopyLayerRender(state.isBaby ? babyBodyModel : bodyModel, texture, poseStack, submitNodeCollector, lightCoords, state, color, 0);
		poseStack.popPose();
	}

}
