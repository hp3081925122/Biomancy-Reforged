package com.github.elenterius.biomancy.client.render.item;

import net.minecraft.client.resources.PlayerSkin;
import com.github.elenterius.biomancy.mixin.accessor.PlayerRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Map;

public class GeoItemWithArmRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {

	public GeoItemWithArmRenderer(GeoModel<T> model) {
		super(model);
	}

	private static PlayerRenderer getPlayerRenderer(LocalPlayer player) {
		EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

		if (renderDispatcher.getRenderer(player) instanceof PlayerRenderer playerRenderer) return playerRenderer;

		Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers = renderDispatcher.getSkinMap();

		PlayerSkin.Model skinModel = DefaultPlayerSkin.get(player.getUUID()).model();
		if (playerRenderers.get(skinModel) instanceof PlayerRenderer playerRenderer) return playerRenderer;

		return (PlayerRenderer) playerRenderers.get(PlayerSkin.Model.WIDE);
	}

	protected static void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LocalPlayer player, boolean isRightArm) {
		float direction = isRightArm ? 1f : -1f;
		poseStack.mulPose(Axis.YP.rotationDegrees(90f * direction));
		poseStack.translate(1f / 16f * direction, 0f, 0f);

		renderHandWithEmptyPose(poseStack, buffer, packedLight, player, isRightArm);
	}

	protected static void renderHandWithEmptyPose(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LocalPlayer player, boolean isRightArm) {
		PlayerRenderer playerRenderer = getPlayerRenderer(player);
		PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();
		ModelPart armModel = isRightArm ? playerModel.rightArm : playerModel.leftArm;
		ModelPart armSleeveModel = isRightArm ? playerModel.rightSleeve : playerModel.leftSleeve;

		((PlayerRendererAccessor) playerRenderer).biomancy$SetModelProperties(player);
		playerModel.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		playerModel.leftArmPose = HumanoidModel.ArmPose.EMPTY;

		armModel.setRotation(0f, 0f, 0f);
		armModel.setPos(0f, 0f, 0f);
		armSleeveModel.setRotation(0f, 0f, 0f);
		armSleeveModel.setPos(0f, 0f, 0f);

		armModel.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(player.getSkin().texture())), packedLight, OverlayTexture.NO_OVERLAY);
		armSleeveModel.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())), packedLight, OverlayTexture.NO_OVERLAY);
	}

	@Override
	public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
		if (bone.getName().equals("arm_placeholder")) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (renderPerspective.firstPerson() && player != null && !player.isInvisible()) {
				poseStack.pushPose();

				RenderUtil.prepMatrixForBone(poseStack, bone);
				RenderUtil.translateAndRotateMatrixForBone(poseStack, bone);
				renderPlayerArm(poseStack, bufferSource, packedLight, player, renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
				poseStack.popPose();

				bufferSource.getBuffer(renderType);
			}
		}
		else {
			super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
		}
	}

}
