package com.github.elenterius.biomancy.block.chrysalis;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import com.github.elenterius.biomancy.init.tags.ModEntityTags;
import com.github.elenterius.biomancy.util.MobUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface Chrysalis {
	String ENTITY_KEY = "entity_info";
	String ENTITY_NAME_KEY = "name";
	String ENTITY_DATA_KEY = "data";
	String ENTITY_VOLUME_KEY = "volume";

	static boolean isCapturingSupported(Entity entity) {
		if (entity instanceof Mob && entity.isAlive()) {
			EntityType<?> entityType = entity.getType();
			return entityType.canSummon() && entityType.canSerialize() && !entityType.builtInRegistryHolder().is(ModEntityTags.C_CAPTURING_NOT_SUPPORTED);
		}
		return false;
	}

	static boolean isCapturingAllowed(Entity entity) {
		return !entity.is(ModEntityTags.CAPTURING_BY_CHRYSALIS_NOT_ALLOWED);
	}

	static boolean storeEntity(CompoundTag tag, Entity entity, boolean removeEntity) {

		if (removeEntity && entity.isPassenger()) {
			entity.removeVehicle();
			if (entity.isPassenger()) return false;
		}

		List<Entity> cachedPassengers = null;
		if (entity.isVehicle()) {
			cachedPassengers = entity.getPassengers();
			entity.ejectPassengers();
		}

		if (saveEntity(tag, entity)) {
			if (removeEntity) {
				entity.remove(Entity.RemovalReason.DISCARDED);
			}
			else if (cachedPassengers != null) {
				cachedPassengers.forEach(passenger -> passenger.startRiding(entity));
			}
			return true;
		}

		return false;
	}

	static boolean saveEntity(CompoundTag compoundTag, Entity entity) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());

		if (entity.saveAsPassenger(output)) {
			CompoundTag entityData = output.buildResult();
			CompoundTag tag = new CompoundTag();
			tag.put(ENTITY_DATA_KEY, entityData);
			tag.putString(ENTITY_NAME_KEY, entity.getType().getDescriptionId());

			EntityDimensions dimensions = entity.getDimensions(entity.getPose());
			float volume = dimensions.width() * dimensions.height() * dimensions.width();
			tag.putFloat(ENTITY_VOLUME_KEY, volume);

			compoundTag.put(ENTITY_KEY, tag);
			return true;
		}

		return false;
	}

	static boolean spawnEntity(ServerLevel level, Vec3 pos, CompoundTag entityTag) {
		Entity entityToSpawn = EntityType.loadEntityRecursive(entityTag.getCompoundOrEmpty(ENTITY_DATA_KEY), level, EntitySpawnReason.LOAD, entity -> {
			entity.snapTo(pos.x, pos.y, pos.z, Mth.wrapDegrees(level.getRandom().nextFloat() * 360), 0);

			if (entity instanceof LivingEntity living) {
				living.yHeadRot = living.getYRot();
				living.yBodyRot = living.getYRot();
			}

			entity.setDeltaMovement(0, 0, 0);
			entity.fallDistance = 0;
			return entity;
		});

		if (entityToSpawn != null) {
			if (!MobUtil.isEntityIdUnique(level, entityToSpawn)) {
				MobUtil.randomizeUUID(entityToSpawn);
			}
			return level.addFreshEntity(entityToSpawn);
		}

		return false;
	}
}
