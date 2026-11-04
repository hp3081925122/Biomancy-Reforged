package com.github.elenterius.biomancy.client;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.client.ModKeyBindings;
import com.github.elenterius.biomancy.item.KeyPressListener;
import com.github.elenterius.biomancy.network.ModNetworkHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, value = Dist.CLIENT)
public final class ClientInputHandler {

	private static final EquipmentSlot[] armorSlotTypes = Arrays.stream(EquipmentSlot.values()).filter(EquipmentSlot::isArmor).toArray(EquipmentSlot[]::new);

	private ClientInputHandler() {}

	@SubscribeEvent
	public static void onKeyInput(final InputEvent.Key event) {
		if (event.getAction() != GLFW.GLFW_RELEASE) return;

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		InputConstants.Key key = InputConstants.getKey(event.getKeyEvent());

		if (ModKeyBindings.MAIN_HAND_ITEM_ACTION.isActiveAndMatches(key)) {
			handleEquipmentSlot(EquipmentSlot.MAINHAND, player);
		}

		if (ModKeyBindings.OFF_HAND_ITEM_ACTION.isActiveAndMatches(key)) {
			handleEquipmentSlot(EquipmentSlot.OFFHAND, player);
		}

		if (ModKeyBindings.EQUIPPED_ARMOR_ACTION.isActiveAndMatches(key)) {
			handleEquipmentSlots(armorSlotTypes, player);
		}
	}

	private static void handleEquipmentSlots(EquipmentSlot[] slots, LocalPlayer player) {
		for (EquipmentSlot slot : slots) {
			handleEquipmentSlot(slot, player);
		}
	}

	private static void handleEquipmentSlot(EquipmentSlot slot, LocalPlayer player) {
		ItemStack stack = player.getItemBySlot(slot);
		if (!stack.isEmpty() && stack.getItem() instanceof KeyPressListener keyListener) {
			KeyPressListener.KeyPressResult result = keyListener.onClientKeyPress(stack, player.level(), player, slot, KeyPressListener.KeyPressResult.NO_FLAGS);
			if (result.success()) {
				ModNetworkHandler.sendKeyBindPressToServer(slot, result.flags());
			}
		}
	}
}
