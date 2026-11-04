package com.github.elenterius.biomancy.client.render.entity.mob.fleshblob;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.render.BiomancyGeoModel;
import com.github.elenterius.biomancy.entity.mob.fleshblob.FleshBlob;
import com.github.elenterius.biomancy.entity.mob.fleshblob.TumorFlag;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class AbstractFleshBlobRenderer<T extends FleshBlob> extends GeoEntityRenderer<T, LivingEntityRenderState> {

	private static final DataTicket<Float> BLOB_SCALE = DataTicket.create(BiomancyMod.MOD_ID + ":blob_scale", Float.class);
	private static final DataTicket<Integer> TUMOR_FLAGS = DataTicket.create(BiomancyMod.MOD_ID + ":tumor_flags", Integer.class);

	protected AbstractFleshBlobRenderer(EntityRendererProvider.Context context, BiomancyGeoModel<T> model) {
		super(context, model);
		shadowRadius = 0.65f;
	}

	@Override
	public void extractRenderState(T fleshBlob, LivingEntityRenderState renderState, float partialTick) {
		renderState.addGeckolibData(BLOB_SCALE, fleshBlob.getBlobScale());
		super.extractRenderState(fleshBlob, renderState, partialTick);
	}

	@Override
	public void addRenderData(T fleshBlob, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
		renderState.addGeckolibData(TUMOR_FLAGS, (int) fleshBlob.getTumorFlags());
	}

	@Override
	protected float getShadowRadius(LivingEntityRenderState renderState) {
		return 0.65f * renderState.getOrDefaultGeckolibData(BLOB_SCALE, 1f);
	}

	@Override
	public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
		int flag = renderPassInfo.getOrDefaultGeckolibData(TUMOR_FLAGS, 0);
		for (TumorFlag tumorFlag : TumorFlag.values()) {
			if (tumorFlag.isNotSet(flag)) {
				snapshots.ifPresent(tumorFlag.getBoneId(), bone -> bone.skipRender(true).skipChildrenRender(true));
			}
		}
	}

	@Override
	public void scaleModelForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, float widthScale, float heightScale) {
		float blobScale = renderPassInfo.getOrDefaultGeckolibData(BLOB_SCALE, 1f);
		if (blobScale != 1) {
			renderPassInfo.poseStack().scale(blobScale, blobScale, blobScale);
		}
	}

	@Override
	public @Nullable RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
		RenderType renderType = BiomancyGeoModel.getRenderType(renderState);
		if (renderType != null && !renderState.isInvisible) return renderType;
		return super.getRenderType(renderState, texture);
	}

}
