package com.github.elenterius.biomancy.entity.projectile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.github.elenterius.biomancy.util.shooting.ProjectileEntityType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;

public abstract class BaseProjectile extends Projectile implements IEntityWithComplexSpawn {

	public static final float DEFAULT_DAMAGE = 1f;

	protected float damage = DEFAULT_DAMAGE;
	protected byte knockback = 0;

	protected final float baseAirDrag;
	protected final float baseWaterDrag;
	protected final float baseGravity;

	protected BaseProjectile(ProjectileEntityType<? extends BaseProjectile> entityType, Level level) {
		super(entityType, level);
		baseAirDrag = entityType.getAirDrag();
		baseWaterDrag = entityType.getWaterDrag();
		baseGravity = entityType.getGravity();
	}

	@Override
	public ProjectileEntityType<? extends BaseProjectile> getType() {
		return (ProjectileEntityType<? extends BaseProjectile>) super.getType();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		Entity shooter = getOwner();
		buffer.writeVarInt(shooter == null ? 0 : shooter.getId());
		buffer.writeFloat(damage);
		buffer.writeByte(knockback);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf buffer) {
		Entity shooter = level().getEntity(buffer.readVarInt());
		setOwner(shooter);
		damage = buffer.readFloat();
		knockback = buffer.readByte();
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("damage", damage);
		output.putByte("knockback", knockback);
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		damage = input.getFloatOr("damage", DEFAULT_DAMAGE);
		knockback = input.getByteOr("knockback", (byte) 0);
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity, inaccuracy);
	}

	public float getDamage() {
		return damage;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	public int getKnockback() {
		return knockback;
	}

	public void setKnockback(int knockback) {
		this.knockback = (byte) Mth.clamp(knockback, 0, Byte.MAX_VALUE);
	}

	public float getAirDrag() {
		return noPhysics ? 0f : baseAirDrag;
	}

	public float getWaterDrag() {
		return noPhysics ? 0f : baseWaterDrag;
	}

	@Override
	protected double getDefaultGravity() {
		return baseGravity;
	}

	protected double getProjectileGravity() {
		return getGravity();
	}

	protected void doPostAttackEffects(LivingEntity shooter, Entity victim, DamageSource damageSource) {
		if (level() instanceof ServerLevel serverLevel) {
			EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, victim, damageSource, shooter.getMainHandItem());
		}
	}

	@Override
	public void tick() {
		Entity shooter = getOwner();
		if (level().isClientSide() || ((shooter == null || !shooter.isRemoved()) && level().isAreaLoaded(blockPosition(), 1))) {
			super.tick();

			if (isInWaterOrRain()) clearFire();
			HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			if (hitResult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitResult)) {
				onHit(hitResult);
			}
			applyEffectsFromBlocks();

			Vec3 motion = getDeltaMovement();
			double posX = getX() + motion.x;
			double posY = getY() + motion.y;
			double posZ = getZ() + motion.z;
			updateRotation();

			float linearDrag = getAirDrag();
			if (isInWater()) {
				for (int i = 0; i < 4; ++i) {
					level().addParticle(ParticleTypes.BUBBLE, posX - motion.x * 0.25f, posY - motion.y * 0.25f, posZ - motion.z * 0.25f, motion.x, motion.y, motion.z);
				}
				linearDrag = getWaterDrag();
			}

			Vec3 dragForce = motion.scale(-linearDrag);
			double gravity = getProjectileGravity();
			setDeltaMovement(motion.add(dragForce.x, dragForce.y - gravity, dragForce.z));

			spawnParticle(posX, posY, posZ);
			setPos(posX, posY, posZ);
		}
		else {
			discard();
		}
	}

	@Override
	public void onHit(HitResult result) {
		super.onHit(result);
		if (!level().isClientSide()) discard();
	}

	protected void spawnParticle(double x, double y, double z) {
		level().addParticle(getParticle(), x, y + getBbHeight() * 0.5f, z, 0, 0, 0);
	}

	protected ParticleOptions getParticle() {
		return ParticleTypes.SMOKE;
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && !entity.noPhysics;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public float getPickRadius() {
		return 1f;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		double dist = getBoundingBox().getSize() * 10d;
		if (Double.isNaN(dist)) dist = 1d;
		dist = dist * 64d * getViewScale();
		return distance < dist * dist;
	}

}
