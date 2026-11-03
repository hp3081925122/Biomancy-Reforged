package com.github.elenterius.biomancy.event;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.ModCapabilities;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.init.tags.ModDamageTypeTags;
import com.github.elenterius.biomancy.item.CriticalHitListener;
import com.github.elenterius.biomancy.item.armor.LivingArmorItem;
import com.github.elenterius.biomancy.util.OneShotTaskWorker;
import com.github.elenterius.biomancy.util.explosion.ExplosionUtil;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class AttackHandler {

	private AttackHandler() {}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCriticalHit(final CriticalHitEvent event) {
		if (event.getDamageMultiplier() > 0 && event.getTarget() instanceof LivingEntity target && event.isCriticalHit()) {
			ItemStack heldStack = event.getEntity().getMainHandItem();
			if (heldStack.getItem() instanceof CriticalHitListener listener) {
				listener.onCriticalHitEntity(heldStack, event.getEntity(), target);
			}
		}
	}

	@SubscribeEvent
	public static void onHurt(final LivingIncomingDamageEvent event) {
		LivingEntity livingEntity = event.getEntity();

		if (!livingEntity.level().isClientSide && event.getAmount() >= 6f && livingEntity.hasEffect(ModMobEffects.VOLATILE)) {
			OneShotTaskWorker.onNextTick(livingEntity, living -> {
				living.removeEffect(ModMobEffects.VOLATILE);
				float radius = 3f - living.getArmorCoverPercentage() * 1.5f;
				ExplosionUtil.explodeVolatile(living.level(), living, radius, Level.ExplosionInteraction.MOB);
			});
		}

		DamageSource damageSource = event.getSource();
		if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
		if (!damageSource.is(ModDamageTypeTags.C_IS_ACID)) return;
		if (event.getAmount() <= 0f) return;

		int resistProbability = 0;
		for (ItemStack itemStack : livingEntity.getArmorSlots()) {
			if (itemStack.getItem() instanceof LivingArmorItem armor && armor.hasNutrients(itemStack)) {
				resistProbability += 25;
				armor.decreaseNutrients(itemStack, 1);
			}
		}

		if (resistProbability > 0 && livingEntity.getRandom().nextInt(100) <= resistProbability) {
			event.setAmount(event.getAmount() * (1f - resistProbability / 100f));
		}
	}

	@Deprecated(forRemoval = true)
	@SubscribeEvent(receiveCanceled = false)
	public static void onKnockback(final LivingKnockBackEvent event) {
		event.getEntity().getExistingData(ModCapabilities.NO_KNOCKBACK_FLAG_CAP).ifPresent(flag -> {
			if (flag.isEnabled()) {
				flag.disable();
				event.setCanceled(true);
			}
		});
	}

}
