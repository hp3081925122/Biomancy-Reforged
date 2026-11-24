package com.github.elenterius.biomancy.client;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.util.TransliterationUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import org.apache.commons.lang3.StringUtils;

// 仅在客户端监听聊天发送事件，避免专用服加载客户端聊天与本地玩家类
@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class ClientChatMessageHandler {

	private ClientChatMessageHandler() {}

	@SubscribeEvent
	public static void onClientSendChatMessageToServer(final ClientChatEvent event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && player.hasEffect(ModMobEffects.PRIMORDIAL_INFESTATION)) {
			String transliterated = TransliterationUtil.transliterate(event.getMessage());
			String abbreviated = StringUtils.abbreviate(transliterated, SharedConstants.MAX_CHAT_LENGTH);
			event.setMessage(abbreviated);
		}
	}

}
