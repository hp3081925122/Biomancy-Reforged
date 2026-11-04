package com.github.elenterius.biomancy.client.render.item;

import com.geckolib.constant.DataTickets;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.item.BEWLBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class BlockEntityItemSpecialRenderer implements SpecialModelRenderer<BlockEntityRenderState> {

	public static final Identifier ID = BiomancyMod.rl("block_entity");
	private static final CameraRenderState ITEM_CAMERA = new CameraRenderState();

	private final float yOffset;

	public BlockEntityItemSpecialRenderer(float yOffset) {
		this.yOffset = yOffset;
	}

	@Override
	public @Nullable BlockEntityRenderState extractArgument(ItemStack stack) {
		if (!(stack.getItem() instanceof BEWLBlockItem blockItem)) return null;

		BlockEntity blockEntity = blockItem.getCachedBEWL();
		if (blockEntity == null) return null;

		Minecraft minecraft = Minecraft.getInstance();
		float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
		return extractRenderState(minecraft.getBlockEntityRenderDispatcher(), blockEntity, partialTick, cameraPos);
	}

	private static <E extends BlockEntity, S extends BlockEntityRenderState> @Nullable S extractRenderState(BlockEntityRenderDispatcher dispatcher, E blockEntity, float partialTick, Vec3 cameraPos) {
		BlockEntityRenderer<E, S> renderer = dispatcher.getRenderer(blockEntity);
		if (renderer == null) return null;

		S state = renderer.createRenderState();
		renderer.extractRenderState(blockEntity, state, partialTick, cameraPos, null);
		return state;
	}

	@Override
	public void submit(@Nullable BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
		if (state == null) return;

		state.lightCoords = lightCoords;
		state.addGeckolibData(DataTickets.PACKED_LIGHT, lightCoords);
		state.addGeckolibData(DataTickets.PACKED_OVERLAY, overlayCoords);

		Minecraft minecraft = Minecraft.getInstance();
		poseStack.pushPose();
		if (yOffset != 0) poseStack.translate(0f, yOffset, 0f);
		minecraft.getBlockEntityRenderDispatcher().submit(state, poseStack, collector, ITEM_CAMERA);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		output.accept(new Vector3f(0f, 0f, 0f));
		output.accept(new Vector3f(1f, 1f, 1f));
	}

	public record Unbaked(float yOffset) implements SpecialModelRenderer.Unbaked<BlockEntityRenderState> {

		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.FLOAT.optionalFieldOf("y_offset", 0f).forGetter(Unbaked::yOffset)
		).apply(instance, Unbaked::new));

		@Override
		public SpecialModelRenderer<BlockEntityRenderState> bake(SpecialModelRenderer.BakingContext context) {
			return new BlockEntityItemSpecialRenderer(yOffset);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

	}

}
