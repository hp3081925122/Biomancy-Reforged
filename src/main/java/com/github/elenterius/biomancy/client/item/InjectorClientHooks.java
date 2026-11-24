package com.github.elenterius.biomancy.client.item;

import com.github.elenterius.biomancy.client.gui.InjectorScreen;
import com.github.elenterius.biomancy.client.render.item.injector.InjectorRenderer;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;

// 注射器客户端逻辑钩子，将界面、本地玩家与渲染器访问隔离到客户端类
public final class InjectorClientHooks {

	private InjectorClientHooks() {}

	public static void tryToOpenScreen(InteractionHand hand) {
		Screen currScreen = Minecraft.getInstance().screen;
		if (currScreen == null && Minecraft.getInstance().player != null) {
			Minecraft.getInstance().setScreen(new InjectorScreen(hand));
			Minecraft.getInstance().player.playNotifySound(ModSoundEvents.UI_RADIAL_MENU_OPEN.get(), SoundSource.PLAYERS, 1f, 1f);
		}
	}

	public static void onSoundKeyFrame(InjectorItem item, SoundKeyframeEvent<InjectorItem> event) {
		if (GeoRenderProvider.of(item).getGeoItemRenderer() instanceof InjectorRenderer renderer) {
			ItemStack stack = renderer.getCurrentItemStack();
			Level level = Minecraft.getInstance().level;
			if (level == null) return;
			if (!item.getInjectionSuccess(stack)) return;

			Entity soundOrigin = item.getEntityHost(stack, level);
			if (soundOrigin == null) return;

			LocalPlayer client = Minecraft.getInstance().player;
			level.playSound(client, soundOrigin.getX(), soundOrigin.getY(0.5f), soundOrigin.getZ(), ModSoundEvents.INJECTOR_INJECT.get(), SoundSource.PLAYERS, 0.8f, 1f / (level.random.nextFloat() * 0.5f + 1f) + 0.2f);

			Entity victim = item.getEntityVictim(stack, level);
			if (victim != null) {
				level.levelEvent(LevelEvent.PARTICLES_DRAGON_BLOCK_BREAK, victim.blockPosition(), 0);
			}
		}
	}

}
