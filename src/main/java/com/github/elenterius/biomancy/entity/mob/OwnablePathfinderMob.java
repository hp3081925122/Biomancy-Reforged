package com.github.elenterius.biomancy.entity.mob;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.scores.PlayerTeam;
import com.github.elenterius.biomancy.util.ownable.OwnableMob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class OwnablePathfinderMob extends PathfinderMob implements OwnableMob {

	protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(OwnablePathfinderMob.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
	private BlockPos targetBlockPos;

	protected OwnablePathfinderMob(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(OWNER_UNIQUE_ID, Optional.empty());
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		getOptionalOwnerUUID().ifPresent(value -> output.store("OwnerUUID", UUIDUtil.CODEC, value));
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		if (input.read("OwnerUUID", UUIDUtil.CODEC).isPresent()) {
			setOwner(input.read("OwnerUUID", UUIDUtil.CODEC).orElseThrow());
		}
		else removeOwner();
	}

	@Override
	public Optional<UUID> getOptionalOwnerUUID() {
		return entityData.get(OWNER_UNIQUE_ID).map(EntityReference::getUUID);
	}

	@Override
	public void setOwner(UUID uuid) {
		entityData.set(OWNER_UNIQUE_ID, Optional.of(EntityReference.of(uuid)));
	}

	@Override
	public void removeOwner() {
		entityData.set(OWNER_UNIQUE_ID, Optional.empty());
	}

	@Override
	public Optional<Player> getOwnerAsPlayer() {
		return getOptionalOwnerUUID().map(value -> level().getPlayerByUUID(value));
	}

	@Override
	public Optional<LivingEntity> getOwnerAsEntity() {
		return getOptionalOwnerUUID().map(value -> level().getPlayerByUUID(value));
	}

	@Override
	public @Nullable PlayerTeam getTeam() {
		return getOwnerAsPlayer().map(Entity::getTeam).orElseGet(super::getTeam);
	}

	@Override
	protected boolean considersEntityAsAlly(Entity entityIn) {
		Optional<Player> optional = getOwnerAsPlayer();
		if (optional.isPresent()) {
			if (optional.get() == entityIn) return true;
			return optional.get().isAlliedTo(entityIn);
		}
		return super.considersEntityAsAlly(entityIn);
	}

	@Override
	public void die(DamageSource cause) {
		if (level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)) {
			Optional<Player> optional = getOwnerAsPlayer();
			if (optional.isPresent() && optional.get() instanceof ServerPlayer) {
				optional.get().sendSystemMessage(getCombatTracker().getDeathMessage());
			}
		}
		super.die(cause);
	}

	@Override
	public boolean canBeLeashed() {
		return true;
	}

	public boolean canBeLeashedBy(Player player) {
		return !isLeashed() && player == getOwnerAsPlayer().orElse(null);
	}

	@Nullable
	public BlockPos getTargetBlockPos() {
		return targetBlockPos;
	}

	public void setTargetBlockPos(@Nullable BlockPos targetPos) {
		this.targetBlockPos = targetPos;
	}

}
