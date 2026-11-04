package com.github.elenterius.biomancy.util.explosion;

import com.github.elenterius.biomancy.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public enum ExplosionType {
	VANILLA(
			(level, source, damageSource, damageCalculator, x, y, z, radius, fire, interaction) -> new ServerExplosion(level, source, damageSource, damageCalculator, new Vec3(x, y, z), radius, fire, interaction),
			() -> ParticleTypes.EXPLOSION, () -> ParticleTypes.EXPLOSION_EMITTER
	),
	DECAY(DecayExplosion::new, ModParticleTypes.DECAY_EXPLOSION::get, ModParticleTypes.DECAY_EXPLOSION_EMITTER::get),
	VOLATILE(VolatileExplosion::new, ModParticleTypes.VOLATILE_EXPLOSION::get, ModParticleTypes.VOLATILE_EXPLOSION_EMITTER::get);

	public final ServerExplosionFactory<? extends ServerExplosion> serverFactory;
	public final Supplier<? extends ParticleOptions> smallParticle;
	public final Supplier<? extends ParticleOptions> largeParticle;

	<T extends ServerExplosion> ExplosionType(ServerExplosionFactory<T> serverFactory, Supplier<? extends ParticleOptions> smallParticle, Supplier<? extends ParticleOptions> largeParticle) {
		this.serverFactory = serverFactory;
		this.smallParticle = smallParticle;
		this.largeParticle = largeParticle;
	}

	public interface ServerExplosionFactory<T extends ServerExplosion> {
		T create(ServerLevel level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, Explosion.BlockInteraction interaction);
	}

}
