package com.github.elenterius.biomancy.client.render.block;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public abstract class CustomGeoBlockRenderer<T extends BlockEntity & GeoBlockEntity> extends GeoBlockRenderer<T, BlockEntityRenderState> {

	private final Function<Identifier, RenderType> renderType;

	protected CustomGeoBlockRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model, Function<Identifier, RenderType> renderType) {
		super(context, model);
		this.renderType = renderType;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}

	public int getAnimationDistance() {
		return 48;
	}

	public boolean shouldAnimate(T blockEntity) {
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
		return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, getAnimationDistance());
	}

	@Override
	protected Direction getBlockStateDirection(T blockEntity) {
		BlockState blockState = blockEntity.getBlockState();
		if (blockState.hasProperty(HorizontalDirectionalBlock.FACING)) return blockState.getValue(HorizontalDirectionalBlock.FACING);
		if (blockState.hasProperty(DirectionalBlock.FACING)) return blockState.getValue(DirectionalBlock.FACING);
		return Direction.NORTH;
	}

	@Override
	public @Nullable RenderType getRenderType(BlockEntityRenderState renderState, Identifier texture) {
		return renderType.apply(texture);
	}

}
