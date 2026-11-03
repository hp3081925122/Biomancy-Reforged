package com.github.elenterius.biomancy.entity.mob;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class OwnableLeashHandler {

	private OwnableLeashHandler() {}

	@SubscribeEvent
	public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		if (!player.getItemInHand(event.getHand()).is(Items.LEAD)) return;

		Entity target = event.getTarget();
		boolean allowed;
		if (target instanceof OwnableMonster ownable) allowed = ownable.getLeashHolder() == player || ownable.canBeLeashedBy(player);
		else if (target instanceof OwnablePathfinderMob ownable) allowed = ownable.getLeashHolder() == player || ownable.canBeLeashedBy(player);
		else return;

		if (!allowed) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.PASS);
		}
	}

}
