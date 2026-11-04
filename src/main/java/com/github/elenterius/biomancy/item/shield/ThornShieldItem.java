package com.github.elenterius.biomancy.item.shield;

import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import com.github.elenterius.biomancy.util.animation.ModDataTickets;
import com.github.elenterius.biomancy.util.animation.GeoRenderProviders;
import com.github.elenterius.biomancy.item.ShieldBlockingListener;
import com.github.elenterius.biomancy.styles.TextComponentUtil;
import com.github.elenterius.biomancy.styles.TextStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import com.github.elenterius.geckolibextras.GLibExtras;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.List;
import java.util.function.Consumer;

public class ThornShieldItem extends LivingShieldItem implements ShieldBlockingListener, GeoItem {

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public ThornShieldItem(int maxNutrients, Properties properties) {
		super(maxNutrients, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (level instanceof ServerLevel serverLevel) {
			GeoItem.getOrAssignId(stack, serverLevel);
		}
	}

	@Override
	public void onShieldBlocking(ItemStack shield, LivingEntity user, LivingEntity attacker) {
		attacker.hurt(user.damageSources().thorns(user), 1.5f + user.getRandom().nextInt(4));
	}

	@Override
	public void appendLivingToolTooltip(ItemStack stack, Consumer<Component> tooltip) {
		tooltip.accept(TextComponentUtil.getAbilityText("thorny_hide").withStyle(TextStyles.GRAY));
		tooltip.accept(ComponentUtil.space().append(TextComponentUtil.getAbilityText("thorny_hide.desc")).withStyle(TextStyles.DARK_GRAY));

		super.appendLivingToolTooltip(stack, tooltip);
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		GeoRenderProviders.accept(this, consumer);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		AnimationController<ThornShieldItem> controller = new AnimationController<>("main", state -> {

			if (state.getData(GLibExtras.ITEM_HOST_TICKET) instanceof LivingEntity livingEntity) {
				if (livingEntity.isBlocking() && livingEntity.getUseItem() == state.getData(ModDataTickets.ITEMSTACK)) {
					return state.setAndContinue(Animations.TRANSITION_TO_EXTENDED);
				}
			}

			if (state.controller().getCurrentRawAnimation() != null && !state.isCurrentAnimationStage("retracted")) {
				return state.setAndContinue(Animations.TRANSITION_TO_RETRACTED);
			}

			return state.setAndContinue(Animations.RETRACTED);
		});

		controllers.add(controller);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	protected static final class Animations {
		public static final RawAnimation RETRACTED = RawAnimation.begin().thenPlay("retracted");
		public static final RawAnimation TRANSITION_TO_RETRACTED = RawAnimation.begin().thenPlay("retract").thenPlay("retracted");
		public static final RawAnimation TRANSITION_TO_EXTENDED = RawAnimation.begin().thenPlay("extend").thenPlay("extended");

		private Animations() {}

	}

}
