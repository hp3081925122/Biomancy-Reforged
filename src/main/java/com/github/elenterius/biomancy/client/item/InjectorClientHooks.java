package com.github.elenterius.biomancy.client.item;

import com.github.elenterius.biomancy.client.gui.InjectorScreen;
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

// 注射器客户端逻辑钩子，将界面与本地玩家访问隔离到客户端类
public final class InjectorClientHooks {

	private InjectorClientHooks() {}

	public static void tryToOpenScreen(InteractionHand hand) {
		Screen currScreen = Minecraft.getInstance().screen;
		if (currScreen == null && Minecraft.getInstance().player != null) {
			Minecraft.getInstance().setScreen(new InjectorScreen(hand));
			LocalPlayer localPlayer = Minecraft.getInstance().player;
			localPlayer.level().playLocalSound(localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), ModSoundEvents.UI_RADIAL_MENU_OPEN.get(), SoundSource.PLAYERS, 1f, 1f, false);
		}
	}

	public static void onSoundKeyFrame(InjectorItem item, ItemStack stack, String soundId, double animationTick) {
		Level level = Minecraft.getInstance().level;
		if (level == null) return;
		if (!item.getInjectionSuccess(stack)) return;

		Entity soundOrigin = item.getEntityHost(stack, level);
		if (soundOrigin == null) return;

		LocalPlayer client = Minecraft.getInstance().player;
		level.playSound(client, soundOrigin.getX(), soundOrigin.getY(0.5f), soundOrigin.getZ(), ModSoundEvents.INJECTOR_INJECT.get(), SoundSource.PLAYERS, 0.8f, 1f / (level.getRandom().nextFloat() * 0.5f + 1f) + 0.2f);

		Entity victim = item.getEntityVictim(stack, level);
		if (victim != null) {
			level.levelEvent(LevelEvent.PARTICLES_DRAGON_BLOCK_BREAK, victim.blockPosition(), 0);
		}
	}

}
