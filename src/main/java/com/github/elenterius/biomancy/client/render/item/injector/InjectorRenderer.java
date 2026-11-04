package com.github.elenterius.biomancy.client.render.item.injector;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.render.item.GeoItemWithArmRenderer;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.jspecify.annotations.Nullable;

public class InjectorRenderer extends GeoItemWithArmRenderer<InjectorItem> {

	private static final String SERUM_CORE_BONE = "_serum_core";
	private static final DataTicket<Integer> SERUM_COLOR = DataTicket.create(BiomancyMod.MOD_ID + ":injector_serum_color", Integer.class);

	public InjectorRenderer() {
		super(new InjectorModel(), RenderTypes::entityTranslucent);
	}

	@Override
	public void addRenderData(InjectorItem item, GeoItemRenderer.@Nullable RenderData renderData, GeoRenderState renderState, float partialTick) {
		super.addRenderData(item, renderData, renderState, partialTick);
		if (renderData == null) return;
		renderState.addGeckolibData(SERUM_COLOR, item.getSerumColor(renderData.itemStack()));
	}

	@Override
	public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
		super.adjustModelBonesForRender(renderPassInfo, snapshots);
		snapshots.ifPresent(SERUM_CORE_BONE, bone -> bone.skipRender(true));
	}

	@Override
	public void preRenderPass(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
		super.preRenderPass(renderPassInfo, renderTasks);
		if (!renderPassInfo.willRender()) return;

		int serumColor = renderPassInfo.getOrDefaultGeckolibData(SERUM_COLOR, -1);
		if (serumColor == -1) return;

		GeoRenderState renderState = renderPassInfo.renderState();
		RenderType renderType = getRenderType(renderState, getTextureLocation(renderState));
		if (renderType == null) return;

		renderPassInfo.model().getBone(SERUM_CORE_BONE).ifPresent(serumBone -> renderPassInfo.addPerBoneRender(serumBone, (passInfo, bone, collector) -> {
			if (!(bone instanceof CuboidGeoBone cuboidBone)) return;

			int color = (passInfo.renderColor() & 0xFF000000) | (serumColor & 0x00FFFFFF);
			int packedLight = passInfo.packedLight();
			int packedOverlay = passInfo.packedOverlay();

			PoseStack poseStack = passInfo.poseStack();
			bone.translateAwayFromPivotPoint(poseStack);

			collector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
				PoseStack cubePoseStack = new PoseStack();
				cubePoseStack.last().set(pose);
				for (GeoCube cube : cuboidBone.cubes) {
					cubePoseStack.pushPose();
					cube.render(cubePoseStack, vertexConsumer, packedLight, packedOverlay, color);
					cubePoseStack.popPose();
				}
			});
		}));
	}

}
