package com.github.elenterius.biomancy.client.render;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.client.ModRenderTypes;
import com.mojang.blaze3d.shaders.Uniform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class PartyTimeShaderHandler {

	private static int ticks = 0;
	private static Uniform time;

	private PartyTimeShaderHandler() {}

	private static Uniform getTimeUniform() {
		if (time == null) {
			time = ModRenderTypes.getEntityCutoutPartyTimeShader().getUniform("Time");
		}
		return time;
	}

	@SubscribeEvent
	static void onClientTick(final ClientTickEvent.Pre event) {
		ticks++;
	}

	@SubscribeEvent
	static void onRenderTick(final RenderFrameEvent.Pre event) {
		float totalTicks = ticks + event.getPartialTick().getGameTimeDeltaPartialTick(true);
		float t = totalTicks * 0.05f;
		getTimeUniform().set(t);
	}

}
