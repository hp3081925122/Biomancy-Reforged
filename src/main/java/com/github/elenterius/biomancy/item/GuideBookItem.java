package com.github.elenterius.biomancy.item;

import com.github.elenterius.biomancy.client.item.GuideBookClientHooks;

import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import net.minecraft.world.InteractionResult;
import com.github.elenterius.geckolibextras.GLibExtras;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
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
		if (FMLEnvironment.getDist().isClient()) {
			GuideBookClientHooks.openScreen(player);
		}
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		if (!FMLEnvironment.getDist().isClient()) return;
		GuideBookClientHooks.registerControllers(controllers, Animations.CLOSED_IDLE_ANIM, Animations.OPEN_THEN_IDLE_ANIM, Animations.CLOSE_THEN_IDLE_ANIM);
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
