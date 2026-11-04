package com.github.elenterius.biomancy.entity.misc;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.particles.ParticleTypes;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModEntityTypes;
import com.github.elenterius.biomancy.init.ModParticleTypes;
import com.github.elenterius.biomancy.mixin.accessor.EntityAccessor;
import com.github.elenterius.biomancy.world.DynamicGasVolume;
import com.github.elenterius.biomancy.util.debug.DebugInfo;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class GasCloud extends Entity implements TraceableEntity, DebugInfo.EntityContext {

	public static final Predicate<Entity> NO_SPECTATOR_BUT_ALIVE_AND_AFFECTED_BY_POTIONS = entity -> entity.isAlive() && !entity.isSpectator() && entity instanceof LivingEntity livingEntity && livingEntity.isAffectedByPotions();
	public static final float DEFAULT_RADIUS = 3f;
	public static final float MIN_RADIUS = 0.5f;
	public static final float MAX_RADIUS = 8f;
	public static final int DEFAULT_PROPAGATION_DURATION = 20 * 3;
	public static final SimpleParticleType DEFAULT_PARTICLE = ModParticleTypes.TOXIN_GAS.get();

	protected static final EntityDataAccessor<Float> RADIUS_DATA = SynchedEntityData.defineId(GasCloud.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Integer> PROPAGATION_DURATION_DATA = SynchedEntityData.defineId(GasCloud.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Boolean> IS_IDLE_DATA = SynchedEntityData.defineId(GasCloud.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<ParticleOptions> PARTICLE_DATA = SynchedEntityData.defineId(GasCloud.class, EntityDataSerializers.PARTICLE);

	private final List<MobEffectInstance> effects = new ArrayList<>();
	private final Object2IntMap<Entity> reapplyCooldowns = new Object2IntOpenHashMap<>();
	private GasInteractionType gasInteractionType = GasInteractionType.INHALATION;

	private int durationTicks = 20 * 30;
	private int effectReapplyCooldown = 20;
	private int durationModificationPerProc;
	private float radiusModificationPerProc;
	private float radiusModificationPerTick;

	private @Nullable DynamicGasVolume dynamicGasVolume;
	private @Nullable LivingEntity owner;
	private @Nullable UUID ownerUUID;

	public GasCloud(EntityType<? extends GasCloud> entityType, Level level) {
		super(entityType, level);
		noPhysics = true;
	}

	public GasCloud(Level level, Vec3 pos) {
		this(ModEntityTypes.GAS_CLOUD.get(), level);
		setPos(pos);
	}

	public GasCloud(Level level, BlockPos pos) {
		this(level, pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
	}

	public GasCloud(Level level, double x, double y, double z) {
		this(ModEntityTypes.GAS_CLOUD.get(), level);
		setPos(x, y, z);
	}

	protected @Nullable ParticleOptions readParticleData(ValueInput input, String key) {
		return input.read(key, ParticleTypes.CODEC).orElse(null);
	}

	protected void writeParticleData(ValueOutput output, String key, ParticleOptions particleOptions) {
		output.store(key, ParticleTypes.CODEC, particleOptions);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(RADIUS_DATA, DEFAULT_RADIUS);
		builder.define(PROPAGATION_DURATION_DATA, DEFAULT_PROPAGATION_DURATION);
		builder.define(IS_IDLE_DATA, true);
		builder.define(PARTICLE_DATA, DEFAULT_PARTICLE);
	}

	public float getRadius() {
		return getEntityData().get(RADIUS_DATA);
	}

	public void setRadius(float radius) {
		getEntityData().set(RADIUS_DATA, Mth.clamp(radius, 0f, MAX_RADIUS));
	}

	public int getDuration() {
		return durationTicks;
	}

	public void setDuration(int ticks) {
		durationTicks = ticks;
	}

	public int getPropagationDuration() {
		return getEntityData().get(PROPAGATION_DURATION_DATA);
	}

	public void setPropagationDuration(int durationTicks) {
		getEntityData().set(PROPAGATION_DURATION_DATA, Math.max(durationTicks, 0));
	}

	public GasInteractionType getInteractionType() {
		return gasInteractionType;
	}

	public void setInteractionType(GasInteractionType type) {
		gasInteractionType = type;
	}

	public int getEffectReapplyCooldown() {
		return effectReapplyCooldown;
	}

	public void setEffectReapplyCooldown(int effectReapplyCooldown) {
		this.effectReapplyCooldown = effectReapplyCooldown;
	}

	public void setDurationModificationPerProc(int durationModificationPerProc) {
		this.durationModificationPerProc = durationModificationPerProc;
	}

	public void setRadiusModificationPerTick(float radiusModificationPerTick) {
		this.radiusModificationPerTick = radiusModificationPerTick;
	}

	public void setRadiusModificationPerProc(float radiusModificationPerProc) {
		this.radiusModificationPerProc = radiusModificationPerProc;
	}

	public void addEffect(MobEffectInstance effectInstance) {
		effects.add(effectInstance);
	}

	public ParticleOptions getParticle() {
		return getEntityData().get(PARTICLE_DATA);
	}

	public void setParticle(ParticleOptions particleOptions) {
		getEntityData().set(PARTICLE_DATA, particleOptions);
	}

	@Override
	public void tick() {
		final boolean isFirstTick = firstTick;

		super.tick();

		if (level().isClientSide()) {
			clientTick();
		}
		else {
			serverTick();
		}

		if (isRemoved()) return;

		int propagationDuration = getPropagationDuration();
		int tickInterval = tickCount < propagationDuration ? 5 : 20;

		if (isFirstTick || tickCount % tickInterval == 0) {
			spreadGas(propagationDuration);
		}
	}

	protected void serverTick() {
		int propagationDurationTicks = getPropagationDuration();

		if (tickCount >= propagationDurationTicks + durationTicks) {
			discard();
			return;
		}

		if (tickCount < Math.min(propagationDurationTicks, 20)) return;

		float radius = getRadius();

		if (tickCount > propagationDurationTicks && radiusModificationPerTick != 0f) {
			radius += radiusModificationPerTick;
			if (radius < 0.5f) {
				discard();
				return;
			}
			setRadius(radius);
		}

		if (tickCount % 5 != 0) return;
		if (!(level() instanceof ServerLevel serverLevel)) return;

		if (effects.isEmpty()) {
			reapplyCooldowns.clear();
			return;
		}

		if (dynamicGasVolume == null) return;

		reapplyCooldowns.object2IntEntrySet().removeIf(entry -> tickCount >= entry.getIntValue());

		List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox(), NO_SPECTATOR_BUT_ALIVE_AND_AFFECTED_BY_POTIONS);
		if (entities.isEmpty()) return;

		for (LivingEntity livingEntity : entities) {
			if (reapplyCooldowns.containsKey(livingEntity)) continue;

			if (gasInteractionType.canInteract(dynamicGasVolume, livingEntity)) {
				reapplyCooldowns.put(livingEntity, tickCount + effectReapplyCooldown);

				for (MobEffectInstance effectInstance : effects) {
					if (effectInstance.getEffect().value().isInstantenous()) {
						effectInstance.getEffect().value().applyInstantenousEffect(serverLevel, this, getOwner(), livingEntity, effectInstance.getAmplifier(), 0.5d);
					}
					else {
						livingEntity.addEffect(new MobEffectInstance(effectInstance), this);
					}
				}

				if (radiusModificationPerProc != 0f) {
					radius += radiusModificationPerProc;
					if (radius < MIN_RADIUS) {
						discard();
						return;
					}
					setRadius(radius);
				}

				if (durationModificationPerProc != 0) {
					durationTicks += durationModificationPerProc;
					if (durationTicks <= 0) {
						discard();
						return;
					}
				}
			}
		}
	}

	protected void clientTick() {
		if (tickCount % 4 != 0) return;
		if (dynamicGasVolume == null || dynamicGasVolume.isEmpty()) return;

		boolean propagationIsNotDone = tickCount < getPropagationDuration();
		LongSet packedPositions = dynamicGasVolume.getPackedPositions();

		ParticleOptions particleoptions = getParticle();
		double x, y, z, xSpeed, ySpeed, zSpeed;

		for (long packedPos : packedPositions) {
			if (propagationIsNotDone) {
				if (random.nextFloat() < 0.35f) continue;
				xSpeed = 0.d;
				ySpeed = 0.d;
				zSpeed = 0.d;
			}
			else {
				if (random.nextFloat() < 0.5f) continue;
				xSpeed = (0.5d - random.nextDouble()) * 0.15d;
				ySpeed = 0.01d;
				zSpeed = (0.5d - random.nextDouble()) * 0.15d;
			}

			x = BlockPos.getX(packedPos) + random.nextDouble();
			y = BlockPos.getY(packedPos) + (0.75d - random.nextDouble());
			z = BlockPos.getZ(packedPos) + random.nextDouble();

			level().addAlwaysVisibleParticle(particleoptions, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}

	private void spreadGas(int propagationDuration) {
		float radius = getRadius();
		if (radius < MIN_RADIUS) {
			if (dynamicGasVolume != null) dynamicGasVolume.clear();
			return;
		}

		if (!isEstimatedChunkAreaLoaded(radius * 1.33f)) return;

		if (dynamicGasVolume == null) {
			long seed = uuid.getMostSignificantBits();
			dynamicGasVolume = new DynamicGasVolume(RandomSource.create(seed));
		}

		if (tickCount < propagationDuration) {
			float t = Mth.clamp(tickCount / (float) propagationDuration, 0f, 1f);
			t = t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2f) / 2f;
			dynamicGasVolume.update(level(), blockPosition(), radius, t);
		}
		else {
			dynamicGasVolume.update(level(), blockPosition(), radius, 1f);
		}

		reapplyPosition();
	}

	private boolean isEstimatedChunkAreaLoaded(float estimatedRadius) {
		double x = getX(), y = getY(), z = getZ();
		int minX = Mth.floor(x - estimatedRadius), minY = Mth.floor(y - estimatedRadius), minZ = Mth.floor(z - estimatedRadius);
		int maxX = Mth.floor(x + estimatedRadius), maxY = Mth.floor(y + estimatedRadius), maxZ = Mth.floor(z + estimatedRadius);

		return level().hasChunksAt(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (RADIUS_DATA.equals(key)) {
			refreshDimensions();
		}
		super.onSyncedDataUpdated(key);
	}

	@Override
	public void refreshDimensions() {
		EntityDimensions dimensions = getDimensions(Pose.STANDING);

		EntityAccessor accessor = (EntityAccessor) this;
		accessor.biomancy$setDimensions(dimensions);
		accessor.biomancy$setEyeHeight(dimensions.eyeHeight());

		reapplyPosition();
	}

	@Override
	protected AABB makeBoundingBox(Vec3 position) {
		if (dynamicGasVolume == null) {
			return new AABB(BlockPos.containing(position));
		}

		return dynamicGasVolume.toAABB();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityDimensions.scalable(getRadius() * 2f, getRadius() * 2f).withEyeHeight(0.5f);
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public @Nullable Entity getOwner() {
		if (owner == null && ownerUUID != null && level() instanceof ServerLevel serverLevel) {
			if (serverLevel.getEntity(ownerUUID) instanceof LivingEntity livingEntity) {
				owner = livingEntity;
			}
		}

		return owner;
	}

	public void setOwner(@Nullable LivingEntity owner) {
		this.owner = owner;
		ownerUUID = owner == null ? null : owner.getUUID();
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		tickCount = input.getIntOr("ticks", 0);
		setPropagationDuration(input.getIntOr("propagation_ticks", 0));
		durationTicks = input.getIntOr("duration_ticks", 0);
		gasInteractionType = GasInteractionType.fromId(input.getByteOr("interaction_type", (byte) 0));
		effectReapplyCooldown = input.getIntOr("effect_reapply_cooldown", 0);
		durationModificationPerProc = input.getIntOr("duration_mod_per_proc", 0);
		radiusModificationPerProc = input.getFloatOr("radius_mod_per_proc", 0f);
		radiusModificationPerTick = input.getFloatOr("radius_per_tick", 0f);
		setRadius(input.getFloatOr("radius", 0f));

		Optional<List<MobEffectInstance>> loadedEffects = input.read("effects", MobEffectInstance.CODEC.listOf());
		if (loadedEffects.isPresent()) {
			effects.clear();
			for (MobEffectInstance effectInstance : loadedEffects.get()) {
				addEffect(effectInstance);
			}
		}

		ParticleOptions particleOptions = readParticleData(input, "particle");
		if (particleOptions != null) {
			setParticle(particleOptions);
		}

		if (input.read("owner", UUIDUtil.CODEC).isPresent()) {
			ownerUUID = input.read("owner", UUIDUtil.CODEC).orElseThrow();
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("ticks", tickCount);
		output.putInt("propagation_ticks", getPropagationDuration());
		output.putInt("duration_ticks", durationTicks);
		output.putByte("interaction_type", gasInteractionType.toId());
		output.putInt("effect_reapply_cooldown", effectReapplyCooldown);
		output.putInt("duration_mod_per_proc", durationModificationPerProc);
		output.putFloat("radius_mod_per_proc", radiusModificationPerProc);
		output.putFloat("radius_per_tick", radiusModificationPerTick);
		output.putFloat("radius", getRadius());
		writeParticleData(output, "particle", getParticle());

		if (!effects.isEmpty()) {
			output.store("effects", MobEffectInstance.CODEC.listOf(), List.copyOf(effects));
		}

		if (ownerUUID != null) {
			output.store("owner", UUIDUtil.CODEC, ownerUUID);
		}
	}

	@Override
	public void emitDebugGizmos(double cameraDistanceSqr, float partialTicks) {
		if (dynamicGasVolume == null || dynamicGasVolume.getPackedPositions().isEmpty()) return;
		if (cameraDistanceSqr >= 64 * 64) return;

		float radius = getRadius();
		int desiredVolume = Mth.floor((4f / 3f) * Mth.PI * (radius * radius * radius * 0.8f));
		int actualVolume = dynamicGasVolume.getPackedPositions().size();

		AABB aabb = getBoundingBox().move(getPosition(partialTicks).subtract(position()));
		Vec3 labelPos = new Vec3(aabb.minX + aabb.getXsize() / 2d, aabb.maxY + 0.25d, aabb.minZ + aabb.getZsize() / 2d);
		Gizmos.billboardText("%d / %d".formatted(actualVolume, desiredVolume), labelPos, TextGizmo.Style.forColorAndCentered(0xFF00FFFF).withScale(0.4f));

		GizmoStyle boxStyle = GizmoStyle.stroke(ARGB.colorFromFloat(1f, 0.25f, 1f, 0f));
		for (long packedPos : dynamicGasVolume.getPackedPositions()) {
			Gizmos.cuboid(BlockPos.of(packedPos), -0.1f, boxStyle);
		}
	}

	public enum GasInteractionType {
		TOUCH((volume, livingEntity) -> volume.intersects(livingEntity.getBoundingBox())),
		INHALATION((volume, livingEntity) -> volume.contains(livingEntity.getEyePosition()));

		private final BiPredicate<DynamicGasVolume, LivingEntity> predicate;

		GasInteractionType(BiPredicate<DynamicGasVolume, LivingEntity> predicate) {
			this.predicate = predicate;
		}

		public static GasInteractionType fromId(byte id) {
			return values()[id];
		}

		public boolean canInteract(DynamicGasVolume gasVolume, LivingEntity livingEntity) {
			return predicate.test(gasVolume, livingEntity);
		}

		public byte toId() {
			return (byte) ordinal();
		}
	}

}
