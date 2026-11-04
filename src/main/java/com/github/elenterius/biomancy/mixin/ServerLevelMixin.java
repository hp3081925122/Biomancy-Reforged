package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.util.random.CellularNoise;
import com.github.elenterius.biomancy.util.random.CellularNoiseProvider;
import com.github.elenterius.biomancy.world.PrimordialEcosystem;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements CellularNoiseProvider {

	@Unique
	private CellularNoise biomancy$cellularNoise = null;

	protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionType, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
		super(levelData, dimension, registryAccess, dimensionType, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Shadow
	public abstract long getSeed();

	@Override
	public CellularNoise biomancy$getCellularNoise() {
		if (biomancy$cellularNoise == null) {
			int seed = (int) (getSeed() + 42 + dimension().identifier().hashCode());
			biomancy$cellularNoise = PrimordialEcosystem.createPreconfiguredCellularNoise(seed);
		}

		return biomancy$cellularNoise;
	}

}
