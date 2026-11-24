package com.github.elenterius.biomancy.client.item;

import com.github.elenterius.biomancy.item.GuideBookItem;
import com.github.elenterius.geckolibextras.GLibExtras;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;

// 指南书客户端逻辑钩子，避免通用物品类直接引用仅客户端类
public final class GuideBookClientHooks {

	private GuideBookClientHooks() {}

	public static void openScreen(Player player) {
		if (player instanceof LocalPlayer localPlayer) {
			Minecraft.getInstance().setScreen(new AdvancementsScreen(localPlayer.connection.getAdvancements()));
		}
	}

	public static void registerControllers(GuideBookItem item, AnimatableManager.ControllerRegistrar controllers, RawAnimation closed, RawAnimation open, RawAnimation close) {
		AnimationController<GuideBookItem> controller = new AnimationController<>(item, "main", state -> {
			Object data = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI ? Minecraft.getInstance().player : state.getData(GLibExtras.ITEM_HOST_TICKET);

			if (data instanceof LivingEntity livingEntity) {
				ItemStack stack = state.getData(DataTickets.ITEMSTACK);
				if (stack == livingEntity.getMainHandItem() || stack == livingEntity.getOffhandItem()) {
					return state.setAndContinue(open);
				}

				if (state.getController().getCurrentAnimation() != null && !state.isCurrentAnimationStage("closed_idle")) {
					return state.setAndContinue(close);
				}
			}

			return state.setAndContinue(closed);
		});

		controllers.add(controller);
	}

}
