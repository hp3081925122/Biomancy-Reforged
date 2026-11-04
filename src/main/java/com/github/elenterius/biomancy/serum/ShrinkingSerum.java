package com.github.elenterius.biomancy.serum;

import com.github.elenterius.biomancy.entity.mob.fleshblob.FleshBlob;
import com.github.elenterius.biomancy.mixin.accessor.ArmorStandAccessor;
import com.github.elenterius.biomancy.mixin.accessor.SlimeAccessor;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.MobUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class ShrinkingSerum extends BasicSerum {

	public ShrinkingSerum(int color) {
		super(color);
	}

	private void resizeArmorStand(ArmorStandAccessor armorStand) {
		armorStand.biomancy$setSmall(true);
	}

	private void resizeFleshBlob(FleshBlob fleshBlob) {
		byte blobSize = fleshBlob.getBlobSize();
		if (blobSize > 1) {
			fleshBlob.setBlobSize((byte) (blobSize - 1), false);
		}
	}

	private void resizeSlime(Slime slime) {
		int slimeSize = slime.getSize();
		if (slimeSize > 1) {
			((SlimeAccessor) slime).biomancy$setSlimeSize(slimeSize - 1, false);
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
		if (target instanceof Slime slime) { // includes MagmaCube
			resizeSlime(slime);
		}
		else if (target instanceof FleshBlob fleshBlob) {
			resizeFleshBlob(fleshBlob);
		}
		else if (target instanceof ArmorStand armorStand && !armorStand.isSmall()) {
			resizeArmorStand((ArmorStandAccessor) armorStand);
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
