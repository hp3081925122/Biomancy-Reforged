package com.github.elenterius.biomancy.client.render.block.tongue;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.RenderPassInfo;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.tongue.TongueBlockEntity;
import com.github.elenterius.biomancy.client.render.block.CustomGeoBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class TongueRenderer extends CustomGeoBlockRenderer<TongueBlockEntity> {

	private static final DataTicket<HeldItem> HELD_ITEM = DataTicket.create(BiomancyMod.MOD_ID + ":tongue_held_item", HeldItem.class);

	private final RandomSource random = RandomSource.create();
	private final ItemModelResolver itemModelResolver;

	public TongueRenderer(BlockEntityRendererProvider.Context context) {
		super(context, new TongueModel(), RenderTypes::entityCutoutCull);
		itemModelResolver = context.itemModelResolver();
	}

	@Override
	public void addRenderData(TongueBlockEntity tongue, @Nullable Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
		ItemStack stack = tongue.getHeldItem();
		if (stack.isEmpty()) return;

		ItemStackRenderState itemState = new ItemStackRenderState();
		itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.GROUND, tongue.getLevel(), null, 0);
		int seed = Item.getId(stack.getItem()) + stack.getDamageValue();
		renderState.addGeckolibData(HELD_ITEM, new HeldItem(itemState, Math.min(stack.getCount(), 3), seed));
	}

	@Override
	public void preRenderPass(RenderPassInfo<BlockEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
		super.preRenderPass(renderPassInfo, renderTasks);

		HeldItem heldItem = renderPassInfo.getGeckolibData(HELD_ITEM);
		if (heldItem == null || heldItem.state().isEmpty()) return;

		renderPassInfo.model().getBone("_item").ifPresent(itemBone -> {
			if (itemBone.parent() == null) return;
			renderPassInfo.addPerBoneRender(itemBone.parent(), (passInfo, parentBone, collector) -> {
				PoseStack poseStack = passInfo.poseStack();
				parentBone.translateAwayFromPivotPoint(poseStack);
				renderItems(poseStack, collector, passInfo.packedLight(), passInfo.packedOverlay(), heldItem);
			});
		});
	}

	private void renderItems(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, HeldItem heldItem) {
		random.setSeed(heldItem.seed());

		poseStack.pushPose();
		poseStack.translate(0, 0.385, 0);
		poseStack.mulPose(Axis.XP.rotationDegrees(90));
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		poseStack.scale(0.75f, 0.75f, 0.75f);

		boolean isGUI3d = heldItem.state().usesBlockLight();
		if (isGUI3d) poseStack.translate(0, 0, 0.1);

		for (int i = 0; i < heldItem.count(); i++) {
			poseStack.pushPose();
			if (i > 0) {
				float x = (random.nextFloat() * 2f - 1) * 0.15f;
				float y = (random.nextFloat() * 2f - 1) * 0.15f;
				if (isGUI3d) {
					poseStack.translate(x, y, (random.nextFloat() * 2f - 1) * 0.15f);
				}
				else {
					poseStack.translate(x * 0.5f, y * 0.5f, 0);
				}
			}
			heldItem.state().submit(poseStack, collector, packedLight, packedOverlay, 0);
			poseStack.popPose();
			if (!isGUI3d) poseStack.translate(0, 0, 0.025F);
		}

		poseStack.popPose();
	}

	private record HeldItem(ItemStackRenderState state, int count, int seed) {}

}
