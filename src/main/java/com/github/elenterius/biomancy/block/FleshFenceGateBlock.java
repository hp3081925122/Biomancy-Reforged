package com.github.elenterius.biomancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class FleshFenceGateBlock extends FenceGateBlock {

	private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Block.cube(16, 16, 4));
	private static final Map<Direction.Axis, VoxelShape> OCCLUSION_SHAPES = Shapes.rotateHorizontalAxis(
			Shapes.or(Block.box(0, 5, 7, 2, 16, 9), Block.box(14, 5, 7, 16, 16, 9))
	);

	public FleshFenceGateBlock(Properties properties) {
		super(properties, SoundEvents.FENCE_GATE_OPEN, SoundEvents.FENCE_GATE_CLOSE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING).getAxis());
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state) {
		return OCCLUSION_SHAPES.get(state.getValue(FACING).getAxis());
	}

}
