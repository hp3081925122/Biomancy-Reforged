package com.github.elenterius.biomancy.serum;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jspecify.annotations.Nullable;

public class InsomniaCureSerum extends BasicSerum {

	public static final String DATA_KEY = BiomancyMod.rlStr("insomnia_cure");
	public static final int PROTECTION_TICKS = 2 * 60 * 20;
	protected static final int PHANTOM_SPAWN_THRESHOLD = 72_000;

	public InsomniaCureSerum(int color) {
		super(color);
	}

	@Override
	public boolean canAffectEntity(CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		return target instanceof Player;
	}

	@Override
	public void affectEntity(ServerLevel level, CompoundTag tag, @Nullable LivingEntity source, LivingEntity target) {
		if (target instanceof ServerPlayer player) affectPlayerSelf(level, tag, player);
	}

	@Override
	public boolean canAffectPlayerSelf(CompoundTag tag, Player targetSelf) {
//		if (!targetSelf.level.isClientSide() && !targetSelf.level.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) return false;

		int ticksSinceRest = getTimeSinceRest(targetSelf);
		if (ticksSinceRest <= PHANTOM_SPAWN_THRESHOLD * 0.833f) {
			if (!targetSelf.level().isClientSide()) {
				targetSelf.sendOverlayMessage(TextComponentUtil.getFailureMsgText("not_sleepy"));
			}
			return false;
		}

		return true;
	}

	@Override
	public void affectPlayerSelf(ServerLevel level, CompoundTag tag, ServerPlayer targetSelf) {
		targetSelf.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)); //reset insomnia

		targetSelf.getPersistentData().putLong(DATA_KEY, level.getGameTime());

		targetSelf.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 10 * 20, 0, false, false, false));
		targetSelf.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 20, 0, false, false, false));
	}

	private static int getTimeSinceRest(Player player) {
		StatsCounter statsCounter;
		if (player instanceof ServerPlayer serverPlayer) {
			statsCounter = serverPlayer.getStats();
		}
		else if (player.level().isClientSide() && FMLEnvironment.getDist().isClient()) {
			statsCounter = ClientStatsAccess.getStats(player);
		}
		else {
			BiomancyMod.LOGGER.debug("Cannot resolve StatsCounter for player {} on dist {}", player.getClass().getName(), FMLEnvironment.getDist());
			return 1;
		}

		if (statsCounter == null) return 1;
		return Mth.clamp(statsCounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
	}

	// 将 LocalPlayer 强转隔离到静态内部类，避免专用服字节码验证加载客户端玩家类
	private static final class ClientStatsAccess {
		private ClientStatsAccess() {}

		private static @Nullable StatsCounter getStats(Player player) {
			return player instanceof net.minecraft.client.player.LocalPlayer localPlayer ? localPlayer.getStats() : null;
		}
	}

}
