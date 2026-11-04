package com.github.elenterius.biomancy.client.render.entity.projectile;

import com.github.elenterius.biomancy.entity.projectile.AcidSpitProjectile;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.llama.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LlamaSpitRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class AcidSpitProjectileRenderer extends EntityRenderer<AcidSpitProjectile, LlamaSpitRenderState> {

	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/llama/llama_spit.png");
	private final LlamaSpitModel model;

	public AcidSpitProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new LlamaSpitModel(context.bakeLayer(ModelLayers.LLAMA_SPIT));
	}

	@Override
	public void submit(LlamaSpitRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0, 0.15f, 0);
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90f));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
		int color = ARGB.opaque(ModMobEffects.CORROSIVE.get().getColor());
		submitNodeCollector.submitModel(model, state, poseStack, model.renderType(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	@Override
	public LlamaSpitRenderState createRenderState() {
		return new LlamaSpitRenderState();
	}

	@Override
	public void extractRenderState(AcidSpitProjectile entity, LlamaSpitRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.xRot = entity.getXRot(partialTick);
		state.yRot = entity.getYRot(partialTick);
	}

}
