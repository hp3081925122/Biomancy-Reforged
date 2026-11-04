package com.github.elenterius.biomancy.event;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModEntityTypes;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.world.MobSpawnFilter;
import com.github.elenterius.spatialdb.SpatialDBManager;
import com.github.elenterius.spatialdb.SpatialQueryStrategy;
import com.github.elenterius.spatialdb.geometry.Shape;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.function.Predicate;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class MobSpawnHandler {

	private MobSpawnHandler() {}

	public static void onCheckSpawn(final MobSpawnEvent.PositionCheck event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			EntitySpawnReason spawnReason = event.getSpawnType();

			if (MobSpawnFilter.isNaturalSpawn(spawnReason)) {

				Mob mob = event.getEntity();

				double x = event.getX();
				double y = event.getY();
				double z = event.getZ();

				Predicate<Shape> denySpawnPredicate = shape -> shape instanceof MobSpawnFilter filter && !filter.isMobAllowedToSpawn(mob, spawnReason, serverLevel, x, y, z);
				boolean denySpawn = SpatialDBManager.getInstance(serverLevel).getAnyShape(serverLevel, mob, SpatialQueryStrategy.INTERSECTION, denySpawnPredicate) != null;

				if (denySpawn) {
					event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBabySpawn(final BabyEntitySpawnEvent event) {
		Mob parentA = event.getParentA();
		Mob parentB = event.getParentB();
		AgeableMob child = event.getChild();

		if (child instanceof Pig) {
			float p = (parentA.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f) + (parentB.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f);
			if (p > 0f && parentA.getRandom().nextFloat() < p) {
				event.setChild(ModEntityTypes.FLESH_PIG.get().create(parentA.level(), EntitySpawnReason.BREEDING));
			}
		}
		else if (child instanceof Cow) {
			float p = (parentA.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f) + (parentB.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f);
			if (p > 0f && parentA.getRandom().nextFloat() < p) {
				event.setChild(ModEntityTypes.FLESH_COW.get().create(parentA.level(), EntitySpawnReason.BREEDING));
			}
		}
		else if (child instanceof Chicken) {
			float p = (parentA.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f) + (parentB.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f);
			if (p > 0f && parentA.getRandom().nextFloat() < p) {
				event.setChild(ModEntityTypes.FLESH_CHICKEN.get().create(parentA.level(), EntitySpawnReason.BREEDING));
			}
		}
		else if (child instanceof Sheep) {
			float p = (parentA.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f) + (parentB.hasEffect(ModMobEffects.LIBIDO) ? 0.1f : 0f);
			if (p > 0f) {
				if (parentA.getRandom().nextFloat() < p) {
					event.setChild(ModEntityTypes.FLESH_SHEEP.get().create(parentA.level(), EntitySpawnReason.BREEDING));
				}
				else if (parentA.getRandom().nextFloat() < p) {
					event.setChild(ModEntityTypes.THICK_FUR_SHEEP.get().create(parentA.level(), EntitySpawnReason.BREEDING));
				}
				else if (parentA.getRandom().nextFloat() < p * 0.5f) {
					event.setChild(ModEntityTypes.CHROMA_SHEEP.get().create(parentA.level(), EntitySpawnReason.BREEDING));
				}
			}
		}
	}

}
