package com.github.elenterius.biomancy.util;

import com.github.elenterius.biomancy.util.MobUtil;
import com.github.elenterius.biomancy.item.armor.LivingArmorItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.util.Mth;
import com.github.elenterius.biomancy.util.EnchantmentUtil;
import com.github.elenterius.biomancy.init.ModDamageSources;
import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.item.armor.AcolyteArmorItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.CommonHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class CombatUtil {
	private CombatUtil() {}

	public static boolean isInvulnerableTo(Entity target, DamageSource source) {
		if (target instanceof LivingEntity livingEntity && target.level() instanceof ServerLevel serverLevel) {
			return livingEntity.isInvulnerableTo(serverLevel, source);
		}
		boolean isVanillaInvulnerable = target.isRemoved()
				|| target.isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isCreativePlayer()
				|| source.is(DamageTypeTags.IS_FIRE) && target.fireImmune()
				|| source.is(DamageTypeTags.IS_FALL) && target.is(EntityTypeTags.FALL_DAMAGE_IMMUNE);
		return CommonHooks.isEntityInvulnerableTo(target, source, isVanillaInvulnerable);
	}

	public static boolean canPierceThroughArmor(ItemStack weapon, LivingEntity target, @Nullable LivingEntity attacker) {
		float pierceProbability = 0;
		for (ItemStack itemStack : MobUtil.getArmorSlots(target)) {
			if (itemStack.getItem() instanceof AcolyteArmorItem) {
				pierceProbability += 0.25f;
			}
		}

		if (attacker != null) {
			for (ItemStack itemStack : MobUtil.getArmorSlots(attacker)) {
				if (itemStack.getItem() instanceof AcolyteArmorItem) {
					pierceProbability += 0.25f;
				}
			}
		}

		int pierceLevel = EnchantmentUtil.getLevel(weapon, Enchantments.PIERCING);
		float pct = getDamageAfterAbsorb(20f, target.getArmorValue(), (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS)) / 20f;
		return target.getRandom().nextFloat() < pct + 0.075f * pierceLevel + pierceProbability;
	}

	public static boolean hasFulLArmorSetEquipped(Player player, Predicate<ItemStack> predicate) {
		Set<EquipmentSlot> equippedTypes = EnumSet.noneOf(EquipmentSlot.class);

		for (EquipmentSlot slot : LivingArmorItem.ARMOR_SLOTS) {
			ItemStack stackInArmorSlot = player.getItemBySlot(slot);
			Equippable equippable = stackInArmorSlot.get(DataComponents.EQUIPPABLE);
			if (equippable != null && ItemCategoryUtil.isHumanoidArmor(stackInArmorSlot) && predicate.test(stackInArmorSlot)) {
				equippedTypes.add(equippable.slot());
			}
			else return false;
		}

		return equippedTypes.size() == LivingArmorItem.ARMOR_SLOTS.size();
	}

	public static void performWaterAOE(Level level, Entity attacker, double maxDistance) {
		AABB aabb = attacker.getBoundingBox().inflate(maxDistance, maxDistance / 2d, maxDistance);
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, LivingEntity::isSensitiveToWater);
		if (!entities.isEmpty()) {
			double maxDistSq = maxDistance * maxDistance;
			for (LivingEntity victim : entities) {
				if (attacker.distanceToSqr(victim) < maxDistSq) {
					victim.hurt(level.damageSources().indirectMagic(victim, attacker), 1f);
				}
			}
		}
	}

	public static void hurtWithAcid(LivingEntity livingEntity, float damage) {
		livingEntity.hurt(ModDamageSources.acid(livingEntity.level(), null), damage);
		livingEntity.invulnerableTime = 0;
	}

	public static void hurtWithBleed(LivingEntity livingEntity, float damage) {
		livingEntity.hurt(ModDamageSources.bleed(livingEntity.level(), null), damage);
		livingEntity.invulnerableTime = 0;
	}

	public static void applyBleedEffect(LivingEntity livingEntity, int seconds) {
		livingEntity.addEffect(new MobEffectInstance(ModMobEffects.BLEED, seconds * 20, 0, false, false, true));
	}

	public static int getBleedEffectLevel(LivingEntity target) {
		MobEffectInstance effectInstance = target.getEffect(ModMobEffects.BLEED);
		if (effectInstance == null) {
			return 0;
		}
		return effectInstance.getAmplifier() + 1;
	}

	private static float getDamageAfterAbsorb(float damage, float armor, float armorToughness) {
		float toughness = 2f + armorToughness / 4f;
		float effectiveArmor = Mth.clamp(armor - damage / toughness, armor * 0.2f, 20f);
		return damage * (1f - effectiveArmor / 25f);
	}

}
