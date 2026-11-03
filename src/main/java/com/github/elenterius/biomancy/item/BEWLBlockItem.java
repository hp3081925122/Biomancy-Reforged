package com.github.elenterius.biomancy.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public class BEWLBlockItem extends SimpleBlockItem {

	private final Lazy<BlockEntity> cachedBlockEntityWithoutLevel;

	public <T extends Block & EntityBlock> BEWLBlockItem(T block, Properties properties) {
		super(block, properties);
		cachedBlockEntityWithoutLevel = Lazy.of(() -> block.newBlockEntity(BlockPos.ZERO, block.defaultBlockState()));
	}

	@Nullable
	public BlockEntity getCachedBEWL() {
		return cachedBlockEntityWithoutLevel.get();
	}

}
