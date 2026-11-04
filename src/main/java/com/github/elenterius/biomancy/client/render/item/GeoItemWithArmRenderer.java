package com.github.elenterius.biomancy.client.render.item;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.function.Function;

public class GeoItemWithArmRenderer<T extends Item & GeoAnimatable> extends BiomancyGeoItemRenderer<T> {

	protected static final String ARM_PLACEHOLDER_BONE = "arm_placeholder";

	public GeoItemWithArmRenderer(GeoModel<T> model) {
		super(model);
	}

	public GeoItemWithArmRenderer(GeoModel<T> model, Function<Identifier, RenderType> renderType) {
		super(model, renderType);
	}

	@Override
	public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
		super.adjustModelBonesForRender(renderPassInfo, snapshots);
		snapshots.ifPresent(ARM_PLACEHOLDER_BONE, bone -> bone.skipRender(true).skipChildrenRender(true));
	}

	@Override
	public void preRenderPass(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
		super.preRenderPass(renderPassInfo, renderTasks);

		ItemDisplayContext perspective = renderPassInfo.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
		if (perspective == null || !perspective.firstPerson()) return;

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || player.isInvisible()) return;

		boolean isRightArm = perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

		renderPassInfo.model().getBone(ARM_PLACEHOLDER_BONE).ifPresent(placeholder -> renderPassInfo.addPerBoneRender(placeholder, (passInfo, bone, collector) -> {
			PoseStack poseStack = passInfo.poseStack();
			bone.translateAwayFromPivotPoint(poseStack);
			RenderUtil.translateAndRotateMatrixForBone(poseStack, bone);
			renderPlayerArm(poseStack, collector, passInfo.packedLight(), player, isRightArm);
		}));
	}

	protected static void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AbstractClientPlayer player, boolean isRightArm) {
		float direction = isRightArm ? 1f : -1f;
		poseStack.mulPose(Axis.YP.rotationDegrees(90f * direction));
		poseStack.translate(1f / 16f * direction, 0f, 0f);

		renderHandWithEmptyPose(poseStack, collector, packedLight, player, isRightArm);
	}

	protected static void renderHandWithEmptyPose(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AbstractClientPlayer player, boolean isRightArm) {
		AvatarRenderer<AbstractClientPlayer> playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
		PlayerModel playerModel = playerRenderer.getModel();

		ModelPart arm = isRightArm ? playerModel.rightArm : playerModel.leftArm;
		ModelPart sleeve = isRightArm ? playerModel.rightSleeve : playerModel.leftSleeve;

		arm.resetPose();
		arm.setPos(0f, 0f, 0f);
		arm.setRotation(0f, 0f, 0f);
		arm.visible = true;
		sleeve.resetPose();
		sleeve.visible = player.isModelPartShown(isRightArm ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);

		//26.1 中袖子是手臂的子部件，原版在同一次提交中以半透明一起绘制
		Identifier skinTexture = player.getSkin().body().texturePath();
		collector.submitModelPart(arm, poseStack, RenderTypes.entityTranslucent(skinTexture), packedLight, OverlayTexture.NO_OVERLAY, null);
	}

}
