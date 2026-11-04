package com.github.elenterius.biomancy.client.render.block.storagesac;

import com.github.elenterius.biomancy.block.storagesac.StorageSacBlock;
import com.github.elenterius.biomancy.block.storagesac.StorageSacBlockEntity;
import com.github.elenterius.biomancy.util.ItemStackCounter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageSacRenderer implements BlockEntityRenderer<StorageSacBlockEntity, StorageSacRenderer.StorageSacRenderState> {

	private final RandomSource random = RandomSource.create();
	private final ItemModelResolver itemModelResolver;

	public StorageSacRenderer(BlockEntityRendererProvider.Context context) {
		itemModelResolver = context.itemModelResolver();
	}

	private float randomOffset(float amount) {
		return (random.nextFloat() * 2f - 1) * amount;
	}

	@Override
	public StorageSacRenderState createRenderState() {
		return new StorageSacRenderState();
	}

	@Override
	public void extractRenderState(StorageSacBlockEntity blockEntity, StorageSacRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);

		state.seed = (int) blockEntity.getBlockPos().asLong() + blockEntity.getBlockPos().getX() - 1;
		state.itemTranslator = ItemTranslator.from(StorageSacBlock.getFacing(blockEntity.getBlockState()));
		state.items.clear();

		for (ItemStackCounter.CountedItem countedItem : blockEntity.getItemsForRendering()) {
			ItemStack stack = countedItem.stack();
			ItemStackRenderState itemState = new ItemStackRenderState();
			if (!stack.isEmpty()) {
				itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
			}
			state.items.add(new RenderedItem(itemState, stack.getCount()));
		}
	}

	@Override
	public void submit(StorageSacRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		random.setSeed(state.seed);
		renderItemsOnAxis(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.items, state.itemTranslator, 0.375f);
	}

	private void renderItemsOnAxis(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, List<RenderedItem> items, ItemTranslator itemTranslator, float itemScale) {
		int numberOfItems = items.size();

		for (int i = 0; i < numberOfItems; i++) {
			RenderedItem item = items.get(i);
			if (item.state().isEmpty()) continue;

			poseStack.pushPose();

			float x = 0.5f + randomOffset(0.175f);
			float y = 0.25f + ((i + 1f) / numberOfItems) * 0.5f;
			float z = 0.5f + randomOffset(0.175f);
			itemTranslator.translate(poseStack, x, y, z);

			for (int j = 0; j < item.count(); j++) {
				poseStack.pushPose();
				if (j > 0) {
					float x2 = randomOffset(0.075f);
					float y2 = randomOffset(0.1f);
					float z2 = randomOffset(0.075f);
					itemTranslator.translate(poseStack, x2, y2, z2);
				}
				poseStack.scale(itemScale, itemScale, itemScale);
				item.state().submit(poseStack, collector, packedLight, packedOverlay, 0);
				poseStack.popPose();
			}

			poseStack.popPose();
		}
	}

	public static class StorageSacRenderState extends BlockEntityRenderState {
		int seed;
		ItemTranslator itemTranslator = ItemTranslator.Y_AXIS;
		final List<RenderedItem> items = new ArrayList<>();
	}

	record RenderedItem(ItemStackRenderState state, int count) {}

	enum ItemTranslator {
		X_AXIS((poseStack, x, y, z) -> poseStack.translate(y, x, z)),
		Y_AXIS(PoseStack::translate),
		Z_AXIS((poseStack, x, y, z) -> poseStack.translate(x, z, y));

		private final TranslationFunc translationFunc;

		ItemTranslator(TranslationFunc translationFunc) {
			this.translationFunc = translationFunc;
		}

		static ItemTranslator from(Direction facing) {
			Direction.Axis axis = facing.getAxis();
			if (axis == Direction.Axis.X) return X_AXIS;
			else if (axis == Direction.Axis.Y) return Y_AXIS;
			return Z_AXIS;
		}

		void translate(PoseStack poseStack, float x, float y, float z) {
			translationFunc.translate(poseStack, x, y, z);
		}

		interface TranslationFunc {
			void translate(PoseStack poseStack, float x, float y, float z);
		}
	}

}
