package com.github.elenterius.biomancy.event;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.network.chat.HoverEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class ChatMessageHandler {

	private ChatMessageHandler() {}

	@SubscribeEvent
	public static void onServerReceiveChatMessageFromClient(final ServerChatEvent event) {
		if (event.getPlayer().hasEffect(ModMobEffects.PRIMORDIAL_INFESTATION)) {
			HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, event.getMessage().copy());
			event.setMessage(ComponentUtil.setStyles(event.getMessage(), TextStyles.PRIMORDIAL_RUNES.withHoverEvent(hoverEvent)));
		}
	}

}
