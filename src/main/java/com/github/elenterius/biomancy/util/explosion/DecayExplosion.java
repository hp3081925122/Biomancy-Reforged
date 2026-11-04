package com.github.elenterius.biomancy.util.explosion;

import com.github.elenterius.biomancy.init.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class DecayExplosion extends GenericExplosion {

	public static final float ATTACK_DAMAGE_MULTIPLIER = 0.0625f;
	public static final float KNOCKBACK_MULTIPLIER = 0.0625f;

	public static ExplosionDamageCalculator BLOCK_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
		@Override
		public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluidState) {
			if (state.isAir() && fluidState.isEmpty()) {
				return Optional.empty();
			}

			if (state.is(ModBlockTags.DECAY_DESTRUCTIBLE)) {
				float explosionResistance = Math.max(state.getExplosionResistance(level, pos, explosion), fluidState.getExplosionResistance(level, pos, explosion));
				return Optional.of(explosionResistance * 0.25f);
			}

			if (state.canBeReplaced() || state.getExplosionResistance(level, pos, explosion) == 0f) {
				return Optional.empty();
			}

			return Optional.of(3_600_000f);
		}

		@Override
		public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
			return state.is(ModBlockTags.DECAY_DESTRUCTIBLE) || state.canBeReplaced() || state.getExplosionResistance(level, pos, explosion) == 0f;
		}
	};

	public DecayExplosion(ServerLevel level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean ignoredFire, BlockInteraction interaction) {
		super(level, source, damageSource, damageCalculator, x, y, z, radius, false, interaction);
	}

	@Override
	protected void hurtEntity(Entity entity, float amount) {
		entity.hurtServer(level, damageSource, amount * ATTACK_DAMAGE_MULTIPLIER);
	}

	@Override
	protected double getKnockbackStrength(Entity entity, double nearExplosionPercent) {
		return super.getKnockbackStrength(entity, nearExplosionPercent) * KNOCKBACK_MULTIPLIER;
	}

	@Override
	protected void finalizeExplosion() {
		if (interactsWithBlocks()) {
			destroyBlocks();
		}
	}

}
