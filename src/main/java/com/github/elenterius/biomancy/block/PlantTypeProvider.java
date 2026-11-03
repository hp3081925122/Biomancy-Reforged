package com.github.elenterius.biomancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public interface PlantTypeProvider {

	PlantType getPlantType(BlockGetter level, BlockPos pos);

}
