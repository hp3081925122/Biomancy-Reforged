package com.github.elenterius.biomancy.item;

import com.github.elenterius.biomancy.client.item.GuideBookClientHooks;
import com.github.elenterius.biomancy.client.render.item.guidebook.GuideBookRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GuideBookItem extends SimpleItem implements GeoItem {

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public GuideBookItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (level instanceof ServerLevel serverLevel) {
			GeoItem.getOrAssignId(stack, serverLevel);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (level.isClientSide) {
			tryToOpenClientScreen(player);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	private void tryToOpenClientScreen(Player player) {
		if (FMLEnvironment.dist.isClient()) {
			GuideBookClientHooks.openScreen(player);
		}
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		consumer.accept(new GeoRenderProvider() {
			private final GuideBookRenderer renderer = new GuideBookRenderer();

			@Override
			public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
				return renderer;
			}
		});
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		if (!FMLEnvironment.dist.isClient()) return;
		GuideBookClientHooks.registerControllers(this, controllers, Animations.CLOSED_IDLE_ANIM, Animations.OPEN_THEN_IDLE_ANIM, Animations.CLOSE_THEN_IDLE_ANIM);
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
