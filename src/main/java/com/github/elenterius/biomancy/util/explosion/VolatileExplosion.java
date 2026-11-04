package com.github.elenterius.biomancy.util.explosion;

import com.github.elenterius.biomancy.init.ModBlocks;
import com.github.elenterius.biomancy.init.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class VolatileExplosion extends GenericExplosion {

	public static final float ATTACK_DAMAGE_MULTIPLIER = 0.125f;
	public static final float KNOCKBACK_MULTIPLIER = 0.125f;

	public static ExplosionDamageCalculator BLOCK_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {

		@Override
		public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
			if (state.isAir() && fluid.isEmpty()) return Optional.empty();

			boolean isFlammable = Direction.stream().anyMatch(direction -> state.isFlammable(level, pos, direction));
			float multiplier = isFlammable ? 0.5f : 3f;

			return Optional.of(multiplier * Math.max(state.getExplosionResistance(level, pos, explosion), fluid.getExplosionResistance(level, pos, explosion)));
		}

		@Override
		public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
			return super.shouldBlockExplode(explosion, level, pos, state, power);
		}

	};

	public VolatileExplosion(ServerLevel level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean ignoredFire, BlockInteraction interaction) {
		super(level, source, damageSource, damageCalculator, x, y, z, radius, true, interaction);
	}

	@Override
	protected void hurtEntity(Entity entity, float amount) {
		super.hurtEntity(entity, amount * ATTACK_DAMAGE_MULTIPLIER);

		if (amount > 0f && entity.isAlive() && entity instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(ModMobEffects.VOLATILE, 60 * 20));
		}
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

		placeSplatters();
	}

	@Override
	protected boolean destroyBlock(BlockPos pos, BlockState state, List<StackCollector> drops) {
		if (state.is(Blocks.MAGMA_BLOCK)) {
			level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
			return true;
		}
		return false;
	}

	protected void placeSplatters() {
		for (BlockPos pos : toBlow) {
			if (random.nextInt(3) == 0 && level.getBlockState(pos).isAir()) {
				for (Direction direction : Direction.values()) {
					BlockPos neighborPos = pos.relative(direction);
					BlockState neighborState = level.getBlockState(neighborPos);
					if (neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {
						ModBlocks.VOLATILE_SPLATTER.get().spreadSplatter(level, pos, direction.getOpposite(), random);
						break;
					}
				}
			}
		}
	}

}
