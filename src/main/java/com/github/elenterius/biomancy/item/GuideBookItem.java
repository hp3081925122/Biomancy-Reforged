package com.github.elenterius.biomancy.item;

import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.InteractionResult;
import com.github.elenterius.geckolibextras.GLibExtras;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.GeoItem;
import com.geckolib.constant.DataTickets;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GuideBookItem extends SimpleItem implements GeoItem {

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public GuideBookItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (level instanceof ServerLevel serverLevel) {
			GeoItem.getOrAssignId(stack, serverLevel);
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (level.isClientSide()) {
			tryToOpenClientScreen(player);
		}

		return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
	}

	private void tryToOpenClientScreen(Player player) {
		if (player instanceof LocalPlayer localPlayer) {
			Minecraft.getInstance().setScreen(new AdvancementsScreen(localPlayer.connection.getAdvancements()));
		}
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		AnimationController<GuideBookItem> controller = new AnimationController<>("main", state -> {

			Object data = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE) == ItemDisplayContext.GUI ? Minecraft.getInstance().player : state.getData(GLibExtras.ITEM_HOST_TICKET);

			if (data instanceof LivingEntity livingEntity) {
				ItemStack stack = state.getData(ModDataTickets.ITEMSTACK);
				if (stack == livingEntity.getMainHandItem() || stack == livingEntity.getOffhandItem()) {
					return state.setAndContinue(Animations.OPEN_THEN_IDLE_ANIM);
				}

				if (state.controller().getCurrentRawAnimation() != null && !state.isCurrentAnimationStage("closed_idle")) {
					return state.setAndContinue(Animations.CLOSE_THEN_IDLE_ANIM);
				}
			}

			return state.setAndContinue(Animations.CLOSED_IDLE_ANIM);
		});

		controllers.add(controller);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	protected static final class Animations {
		public static final RawAnimation CLOSED_IDLE_ANIM = RawAnimation.begin().thenLoop("closed_idle");
		public static final RawAnimation OPEN_THEN_IDLE_ANIM = RawAnimation.begin().thenPlay("opening").thenLoop("open_idle");
		public static final RawAnimation CLOSE_THEN_IDLE_ANIM = RawAnimation.begin().thenPlay("closing").thenLoop("closed_idle");

		private Animations() {}
	}

}
