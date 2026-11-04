package com.github.elenterius.biomancy.serum;

import com.github.elenterius.biomancy.entity.mob.fleshblob.FleshBlob;
import com.github.elenterius.biomancy.mixin.accessor.ArmorStandAccessor;
import com.github.elenterius.biomancy.mixin.accessor.SlimeAccessor;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.MobUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class EnlargementSerum extends BasicSerum {

	public EnlargementSerum(int color) {
		super(color);
	}

	private void resizeArmorStand(ArmorStand armorStand) {
		((ArmorStandAccessor) armorStand).biomancy$setSmall(false);
	}

	private void resizeFleshBlob(@Nullable LivingEntity source, FleshBlob fleshBlob) {
		byte blobSize = fleshBlob.getBlobSize();
		if (blobSize < 10) {
			fleshBlob.setBlobSize((byte) (blobSize + 1), false);
		}
		else {
			DamageSource explosionDamage = fleshBlob.level().damageSources().explosion(source, fleshBlob);
			fleshBlob.hurt(explosionDamage, fleshBlob.getHealth()); //"explode" fleshBob
		}
	}

	private void resizeSlime(@Nullable LivingEntity source, Slime slime) {
		int slimeSize = slime.getSize();
		if (slimeSize < 25) {
			((SlimeAccessor) slime).biomancy$setSlimeSize(slimeSize + 1, false);
		}
		else {
			DamageSource explosionDamage = slime.level().damageSources().explosion(source, slime);
			slime.hurt(explosionDamage, slime.getHealth()); //"explode" slime
		}
	}

	@Override
	public boolean canAffectEntity(CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		if (!MobUtil.isResizable(target)) {
			if (source instanceof Player player && !player.level().isClientSide()) {
				player.sendOverlayMessage(TextComponentUtil.getFailureMsgText("mob_too_powerful"));
			}
			return false;
		}

		return target instanceof Mob || target instanceof Player;
	}

	@Override
	public void affectEntity(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		if (target instanceof Slime slime) {
			resizeSlime(source, slime);
		}
		else if (target instanceof FleshBlob fleshBlob) {
			resizeFleshBlob(source, fleshBlob);
		}
		else if (target instanceof ArmorStand armorStand && armorStand.isSmall()) {
			resizeArmorStand(armorStand);
		}
	}

	@Override
	public boolean canAffectPlayerSelf(CompoundTag tag, Player targetSelf) {
		if (!MobUtil.isResizable(targetSelf)) {
			targetSelf.sendOverlayMessage(TextComponentUtil.getFailureMsgText("mob_too_powerful"));
			return false;
		}

		return true;
	}

	@Override
	public void affectPlayerSelf(ServerLevel level, CompoundTag tag, ServerPlayer targetSelf) {
	}

}
