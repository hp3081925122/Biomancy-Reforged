package com.github.elenterius.biomancy.world;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

public interface MobSpawnFilter {

	Set<EntitySpawnReason> NATURAL_SPAWN_REASON = Set.of(
			EntitySpawnReason.EVENT,
			EntitySpawnReason.NATURAL,
			EntitySpawnReason.CHUNK_GENERATION,
			EntitySpawnReason.PATROL
	);

	static boolean isNaturalSpawn(EntitySpawnReason spawnReason) {
		return NATURAL_SPAWN_REASON.contains(spawnReason);
	}

	/**
	 * At the moment only checks natural mob spawns
	 */
	boolean isMobAllowedToSpawn(Mob mob, EntitySpawnReason spawnReason, LevelAccessor level, double x, double y, double z);

}
