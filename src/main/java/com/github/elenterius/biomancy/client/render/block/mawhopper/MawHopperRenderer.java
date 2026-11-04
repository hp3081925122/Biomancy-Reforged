package com.github.elenterius.biomancy.client.render.block.mawhopper;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.mawhopper.MawHopperBlock;
import com.github.elenterius.biomancy.block.mawhopper.MawHopperBlockEntity;
import com.github.elenterius.biomancy.client.render.block.CustomGeoBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class MawHopperRenderer extends CustomGeoBlockRenderer<MawHopperBlockEntity> {

	private static final DataTicket<Quaternionf> ROTATION = DataTicket.create(BiomancyMod.MOD_ID + ":maw_hopper_rotation", Quaternionf.class);

	public MawHopperRenderer(BlockEntityRendererProvider.Context context) {
		super(context, new MawHopperModel(), RenderTypes::entityCutoutCull);
	}

	@Override
	public void addRenderData(MawHopperBlockEntity blockEntity, @Nullable Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
		renderState.addGeckolibData(ROTATION, new Quaternionf(MawHopperBlock.getConnection(blockEntity.getBlockState()).getUnsafeQuaternion()));
	}

	@Override
	public void adjustRenderPose(RenderPassInfo<BlockEntityRenderState> renderPassInfo) {
		Quaternionf rotation = renderPassInfo.getGeckolibData(ROTATION);
		if (rotation == null) return;

		PoseStack poseStack = renderPassInfo.poseStack();
		poseStack.translate(0, 0.5, 0);
		poseStack.mulPose(rotation);
		poseStack.translate(0, -0.5, 0);
	}

}
