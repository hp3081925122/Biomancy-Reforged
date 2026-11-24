package com.github.elenterius.biomancy.block.membrane;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.util.LevelUtil;
import com.github.elenterius.biomancy.block.cradle.PrimalEnergyHandler;
import com.github.elenterius.biomancy.block.veins.FleshVeinsBlock;
import com.github.elenterius.biomancy.init.ModBlocks;
import com.github.elenterius.biomancy.util.VectorUtil;
import com.github.elenterius.biomancy.world.PrimordialEcosystem;
import com.github.elenterius.biomancy.world.mound.MoundShape;
import com.github.elenterius.spatialdb.SpatialDBManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpreadingMembraneBlock extends MembraneBlock {

	public SpreadingMembraneBlock(Properties properties, IgnoreEntityCollisionPredicate predicate) {
		super(properties.randomTicks(), predicate);
	}

	// 高版本中 BlockBehaviour.randomTick 不再默认转发到 tick，显式覆写以恢复原始渗透膜在巢穴腔室间的扩散逻辑
	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		tick(state, level, pos, random);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getRandom().nextFloat() >= 0.5f) return;
		if (!level.isAreaLoaded(pos, 2)) return;

		BlockPos targetPos = pos.offset(VectorUtil.randomOffsetInCube3i(random));
		BlockState stateAtTargetPos = level.getBlockState(targetPos);

		if (!stateAtTargetPos.isAir() && !(stateAtTargetPos.getBlock() instanceof FleshVeinsBlock) && !PrimordialEcosystem.isReplaceable(stateAtTargetPos)) return;

		if (SpatialDBManager.getInstance(level).getClosestShape(level, pos, MoundShape.class::isInstance) instanceof MoundShape mound) {
			BlockEntity blockEntity = LevelUtil.getExistingBlockEntity(level, mound.getOrigin());
			if (blockEntity instanceof PrimalEnergyHandler energyHandler && !mound.hasChamberAt(targetPos)) {

				int nextToAnyChamberCount = 0;
				for (Direction direction : Direction.allShuffled(random)) {
					if (mound.hasChamberAt(targetPos.relative(direction))) {
						nextToAnyChamberCount++;
						if (nextToAnyChamberCount > 1) break;
					}
				}

				if (energyHandler.drainPrimalEnergy(4) > 0) {
					if (nextToAnyChamberCount > 1) {
						level.setBlock(targetPos, defaultBlockState(), UPDATE_CLIENTS);
						if (BiomancyMod.LOGGER.isDebugEnabled()) {
							BiomancyMod.LOGGER.debug("SpreadingMembrane expanded membrane from {} to {}, remainingEnergy={}", pos, targetPos, energyHandler.getPrimalEnergy());
						}
					}
					else {
						level.setBlock(targetPos, ModBlocks.MALIGNANT_FLESH.get().defaultBlockState(), UPDATE_CLIENTS);
						boolean spread = PrimordialEcosystem.spreadMalignantVeinsFromSource(level, targetPos, PrimordialEcosystem.MAX_CHARGE_SUPPLIER);
						if (BiomancyMod.LOGGER.isDebugEnabled()) {
							BiomancyMod.LOGGER.debug("SpreadingMembrane placed malignant flesh from {} to {}, spreadVeins={}, remainingEnergy={}", pos, targetPos, spread, energyHandler.getPrimalEnergy());
						}
					}
				}
			}
		}
	}

}
