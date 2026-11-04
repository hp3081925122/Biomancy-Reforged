package com.github.elenterius.biomancy.util.ownable;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface OwnableMob extends Ownable, OwnableEntity {

	Optional<Player> getOwnerAsPlayer();

	Optional<LivingEntity> getOwnerAsEntity();

	@Nullable
	default UUID getOwnerUUID() {
		return getOptionalOwnerUUID().orElse(null);
	}

	@Nullable
	@Override
	default EntityReference<LivingEntity> getOwnerReference() {
		return getOptionalOwnerUUID().map(EntityReference::<LivingEntity>of).orElse(null);
	}

	@Nullable
	@Override
	default LivingEntity getOwner() {
		return getOwnerAsEntity().orElse(null);
	}

	default boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
		if (target.getUUID().equals(owner.getUUID())) return false;

		if (target instanceof OwnableMob mob) {
			return !mob.isOwner(owner);
		}
		else if (target instanceof OwnableEntity entity) {
			EntityReference<LivingEntity> ownerReference = entity.getOwnerReference();
			return ownerReference == null || !owner.getUUID().equals(ownerReference.getUUID());
		}
		else return !(target instanceof Player targetPlayer) || !(owner instanceof Player ownerPlayer) || ownerPlayer.canHarmPlayer(targetPlayer);
	}

	boolean tryToReturnIntoPlayerInventory();

}
