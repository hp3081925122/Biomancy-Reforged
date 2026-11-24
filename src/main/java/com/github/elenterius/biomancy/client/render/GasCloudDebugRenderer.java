package com.github.elenterius.biomancy.client.render;

import com.github.elenterius.biomancy.entity.misc.GasCloud;
import com.github.elenterius.biomancy.world.DynamicGasVolume;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

// 毒气云客户端调试信息渲染器，从 GasCloud 实体类抽离以避免专用服加载客户端渲染类
public final class GasCloudDebugRenderer {

	private GasCloudDebugRenderer() {}

	public static void renderDebugInfo(GasCloud gasCloud, EntityRenderDispatcher renderDispatcher, PoseStack poseStack, MultiBufferSource multiBuffer, int packedLight, float partialTicks) {
		DynamicGasVolume dynamicGasVolume = gasCloud.getDynamicGasVolume();
		if (dynamicGasVolume == null || dynamicGasVolume.getPackedPositions().isEmpty()) return;

		double distanceSqr = renderDispatcher.distanceToSqr(gasCloud);
		if (distanceSqr >= 64 * 64) return;

		float radius = gasCloud.getRadius();
		int desiredVolume = Mth.floor((4f / 3f) * Mth.PI * (radius * radius * radius * 0.8f));
		int actualVolume = dynamicGasVolume.getPackedPositions().size();

		double xOffset = -Mth.lerp(partialTicks, gasCloud.xOld, gasCloud.getX());
		double yOffset = -Mth.lerp(partialTicks, gasCloud.yOld, gasCloud.getY());
		double zOffset = -Mth.lerp(partialTicks, gasCloud.zOld, gasCloud.getZ());

		poseStack.pushPose();
		AABB aabb = gasCloud.getBoundingBox().move(xOffset, yOffset, zOffset);
		poseStack.translate(aabb.minX + aabb.getXsize() / 2d, aabb.minY + aabb.getYsize(), aabb.minZ + aabb.getZsize() / 2d);
		renderLabel(renderDispatcher, poseStack, multiBuffer, packedLight, "%d / %d".formatted(actualVolume, desiredVolume), 0.25f, 0x00FFFF, false);
		poseStack.popPose();

		for (long packedPos : dynamicGasVolume.getPackedPositions()) {
			int x = BlockPos.getX(packedPos);
			int y = BlockPos.getY(packedPos);
			int z = BlockPos.getZ(packedPos);

			float red = 0.25f;
			float green = 1f;

			poseStack.pushPose();
			poseStack.translate(xOffset + x, yOffset + y, zOffset + z);
			LevelRenderer.renderLineBox(poseStack, multiBuffer.getBuffer(RenderType.lines()), 0.1f, 0.1f, 0.1f, 0.9f, 0.9f, 0.9f, red, green, 0f, 1f);
			poseStack.popPose();
		}
	}

	private static void renderLabel(EntityRenderDispatcher renderDispatcher, PoseStack poseStack, MultiBufferSource multiBuffer, int packedLight, String text, float y, int color, boolean translucentBackground) {
		poseStack.pushPose();

		poseStack.translate(0f, y, 0f);
		poseStack.mulPose(renderDispatcher.cameraOrientation());
		// MC 1.21+ 修改了 cameraOrientation() 朝向，X 轴须使用正缩放以免文本背面剔除
		poseStack.scale(0.025f, -0.025f, 0.025f);

		int backgroundColor = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255f) << 24;
		Font font = Minecraft.getInstance().font;
		float x = -font.width(text) / 2f;

		Matrix4f matrix4f = poseStack.last().pose();
		font.drawInBatch(text, x, y, 0x20_ffffff, false, matrix4f, multiBuffer, translucentBackground ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, backgroundColor, packedLight);
		font.drawInBatch(text, x, y, 0xFF_000000 | color, false, matrix4f, multiBuffer, Font.DisplayMode.NORMAL, 0, packedLight);

		poseStack.popPose();
	}

}
