package com.github.elenterius.biomancy.block;

import com.github.elenterius.biomancy.init.ModPlantTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.TriState;

public class FleshBlock extends Block {

	protected final PlantType supportedPlantType;

	public FleshBlock(Properties properties) {
		super(properties);
		supportedPlantType = ModPlantTypes.FLESH;
	}

	public FleshBlock(Properties properties, PlantType supportedPlantType) {
		super(properties);
		this.supportedPlantType = supportedPlantType;
	}

	@Override
	public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plant) {
		if (!(plant.getBlock() instanceof PlantTypeProvider plantable)) return TriState.FALSE;
		PlantType type = plantable.getPlantType(world, pos.relative(facing));
		return type == supportedPlantType ? TriState.TRUE : TriState.FALSE;
	}

}
