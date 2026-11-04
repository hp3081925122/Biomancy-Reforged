package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

import java.util.Set;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class ModVillagerTrades {

	private ModVillagerTrades() {}

	@SubscribeEvent
	public static void onTradeWithVillager(TradeWithVillagerEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			OrganTradeTracker.trackTrade(player, event.getAbstractVillager(), event.getMerchantOffer());
		}
	}

	public static class OrganTradeTracker {

		public static final Set<Item> ORGANS = Set.of(ModItems.GENERIC_MOB_GLAND.get(), ModItems.VOLATILE_GLAND.get(), ModItems.TOXIN_GLAND.get());

		private static void trackTrade(ServerPlayer player, AbstractVillager villager, MerchantOffer merchantOffer) {
			if (ORGANS.contains(merchantOffer.getBaseCostA().getItem())) {
				CriteriaTriggers.TRADE.trigger(player, villager, merchantOffer.getBaseCostA());
			}

			if (ORGANS.contains(merchantOffer.getCostB().getItem())) {
				CriteriaTriggers.TRADE.trigger(player, villager, merchantOffer.getCostB());
			}
		}

	}

}
