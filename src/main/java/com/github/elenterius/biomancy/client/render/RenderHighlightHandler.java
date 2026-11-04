package com.github.elenterius.biomancy.client.render;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.base.DirectionalSlabBlock;
import com.github.elenterius.biomancy.block.property.DirectionalSlabType;
import com.github.elenterius.biomancy.init.client.ModRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class RenderHighlightHandler {

	private static final int PREVIEW_COLOR = ARGB.colorFromFloat(0.7f, 1f, 1f, 1f);

	private RenderHighlightHandler() {}

	@SubscribeEvent
	public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
		if (event.getCamera().entity() instanceof Player player) {
			ItemStack itemStack = player.getMainHandItem();
			if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DirectionalSlabBlock slabBlock) {
				BlockPlaceContext placeContext = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, itemStack, event.getHitResult());
				BlockPos placePos = placeContext.getClickedPos();
				BlockState foundState = player.level().getBlockState(placePos);

				DirectionalSlabType type;
				if (foundState.getBlock() instanceof DirectionalSlabBlock) {
					type = DirectionalSlabType.getHalfFrom(foundState.getValue(DirectionalSlabBlock.TYPE).getFacing().getOpposite());
				}
				else {
					type = DirectionalSlabType.getHalfFrom(placePos, placeContext.getClickLocation(), placeContext.getClickedFace());
				}
				BlockState state = slabBlock.defaultBlockState().setValue(DirectionalSlabBlock.TYPE, type);

				event.addCustomRenderer(new BlockPreviewRenderer(placePos.immutable(), collectQuads(state)));
			}
		}
	}

	private static List<BakedQuad> collectQuads(BlockState state) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).collectParts(RandomSource.create(42L), parts);

		List<BakedQuad> quads = new ArrayList<>();
		for (BlockStateModelPart part : parts) {
			for (Direction direction : Direction.values()) {
				quads.addAll(part.getQuads(direction));
			}
			quads.addAll(part.getQuads(null));
		}
		return List.copyOf(quads);
	}

	private record BlockPreviewRenderer(BlockPos pos, List<BakedQuad> quads) implements CustomBlockOutlineRenderer {

		@Override
		public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
			if (!translucentPass) return false;

			Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
			QuadInstance instance = new QuadInstance();
			instance.setColor(PREVIEW_COLOR);
			instance.setLightCoords(0xF000F0);
			instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

			poseStack.pushPose();
			poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
			VertexConsumer consumer = buffer.getBuffer(ModRenderTypes.blockPreview());
			PoseStack.Pose pose = poseStack.last();
			for (BakedQuad quad : quads) {
				consumer.putBakedQuad(pose, quad, instance);
			}
			poseStack.popPose();
			return false;
		}

	}

}
