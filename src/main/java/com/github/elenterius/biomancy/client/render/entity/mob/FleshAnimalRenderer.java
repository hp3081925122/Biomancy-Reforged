package com.github.elenterius.biomancy.client.render.entity.mob;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public abstract class FleshAnimalRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T, LivingEntityRenderState> {

	protected FleshAnimalRenderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadowRadius) {
		super(context, model);
		this.shadowRadius = shadowRadius;
	}

	@Override
	public void scaleModelForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, float widthScale, float heightScale) {
		if (renderPassInfo.renderState().isBaby) {
			widthScale = 0.5f;
			heightScale = 0.5f;
		}
		super.scaleModelForRender(renderPassInfo, widthScale, heightScale);
	}

	@Override
	public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
		if (shouldTurnHead(renderPassInfo)) {
			LivingEntityRenderState state = renderPassInfo.renderState();
			snapshots.ifPresent("head", head -> {
				setRotX(head, state.xRot * Mth.DEG_TO_RAD);
				setRotY(head, state.yRot * Mth.DEG_TO_RAD);
			});
		}
	}

	protected boolean shouldTurnHead(RenderPassInfo<LivingEntityRenderState> renderPassInfo) {
		return true;
	}

	protected static void setRotX(BoneSnapshot bone, float value) {
		bone.setRotX(value - bone.getBone().baseRotX());
	}

	protected static void setRotY(BoneSnapshot bone, float value) {
		bone.setRotY(value - bone.getBone().baseRotY());
	}

	protected static void setRotZ(BoneSnapshot bone, float value) {
		bone.setRotZ(value - bone.getBone().baseRotZ());
	}

}
