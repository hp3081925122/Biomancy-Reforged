package com.github.elenterius.biomancy.item.injector;

import com.github.elenterius.biomancy.api.serum.Serum;
import com.github.elenterius.biomancy.component.InjectionSchedule;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModEnchantments;
import com.github.elenterius.biomancy.item.armor.AcolyteArmorItem;
import com.github.elenterius.biomancy.util.CombatUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class InjectionScheduler {

	private InjectionScheduler() {}

	public static void schedule(InjectorItem injector, ItemStack stack, Player player, LivingEntity target, int delayInTicks) {
		if (stack.isEmpty() || player.level().isClientSide || injector.getSerum(stack).isEmpty()) return;

		injector.setEntityHost(stack, player);
		injector.setEntityVictim(stack, target);

		injector.setInjectionSuccess(stack, CombatUtil.canPierceThroughArmor(stack, target, player));

		stack.set(ModDataComponents.INJECTION_SCHEDULE, new InjectionSchedule(delayInTicks, player.level().getGameTime()));
	}

	public static void tick(ServerLevel level, InjectorItem injector, ItemStack stack, ServerPlayer player) {
		InjectionSchedule schedule = stack.get(ModDataComponents.INJECTION_SCHEDULE);
		if (schedule == null) return;

		long delayInTicks = schedule.delayInTicks();
		long starTimestamp = schedule.timestamp();
		if (player.level().getGameTime() - starTimestamp > delayInTicks) {
			performScheduledSerumInjection(level, injector, stack, player);
			stack.remove(ModDataComponents.INJECTION_SCHEDULE);
		}
	}

	public static void performScheduledSerumInjection(ServerLevel level, InjectorItem injector, ItemStack stack, ServerPlayer player) {
		Serum serum = injector.getSerum(stack);
		if (serum.isEmpty()) return;

		Entity victim = injector.getEntityVictim(stack, level);
		Entity host = injector.getEntityHost(stack, level);
		boolean injectionSuccess = injector.getInjectionSuccess(stack);

		if (victim instanceof LivingEntity target) {
			if (!injectionSuccess) {
				stack.hurtAndBreak(2, player, p -> {});
				player.broadcastBreakEvent(EquipmentSlot.MAINHAND);
				injector.broadcastAnimation(level, player, stack, InjectorItem.InjectorAnimation.REGROW_NEEDLE);
				player.getCooldowns().addCooldown(stack.getItem(), InjectorItem.COOL_DOWN_TICKS * 2);
				return;
			}

			float damagePct = 1f;
			for (ItemStack itemStack : target.getArmorSlots()) {
				if (itemStack.getItem() instanceof AcolyteArmorItem armor && armor.hasNutrients(itemStack)) {
					damagePct -= 0.25f;
				}
			}

			if (stack.getEnchantmentLevel(ModEnchantments.ANESTHETIC.get()) <= 0) {
				float damage = 0.5f * damagePct;
				if (damage > 0) {
					target.hurt(level.damageSources().sting(player), damage);
				}
			}

			if (host == victim) {
				serum.affectPlayerSelf(level, injector.getSerumData(stack), player);
			}
			else {
				serum.affectEntity(level, injector.getSerumData(stack), player, target);
			}

			injector.consumeSerum(stack, player);
			stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}
	}
}
