package com.github.elenterius.biomancy.client.item;

import com.github.elenterius.biomancy.api.serum.Serum;
import com.github.elenterius.biomancy.client.gui.InjectorScreen;
import com.github.elenterius.biomancy.client.util.ClientTextUtil;
import com.github.elenterius.biomancy.component.InjectorContents;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import java.util.function.Consumer;

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

	public static void appendHoverText(InjectorItem item, ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag isAdvanced) {
		Level level = context.level();
		ClientTextUtil.getItemInfoTooltip(stack).forEach(tooltip);
		tooltip.accept(ComponentUtil.EMPTY_LINE);

		InjectorContents contents = stack.get(ModDataComponents.INJECTOR_CONTENTS);
		if (contents != null) {
			Serum serum = item.getSerum(stack);
			if (!serum.isEmpty()) {
				CompoundTag serumData = item.getSerumData(stack);
				short amount = contents.amount();
				tooltip.accept(ComponentUtil.literal(String.format("%dx ", amount)).append(serum.getDisplayName(serumData)).withStyle(ChatFormatting.GRAY));
				if (ClientTextUtil.showExtraInfo(tooltip)) {
					serum.appendTooltip(serumData, level, tooltip, isAdvanced);
				}
				tooltip.accept(ComponentUtil.EMPTY_LINE);
			}
		}

		tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getDefaultKey(), TextComponentUtil.getActionText("open_inventory")).withStyle(ChatFormatting.DARK_GRAY));
		tooltip.accept(ClientTextUtil.pressButtonTo(ClientTextUtil.getShiftKey().append(" + ").append(ClientTextUtil.getRightMouseKey()), TextComponentUtil.getActionText("self_inject")).withStyle(ChatFormatting.DARK_GRAY));
	}
}
