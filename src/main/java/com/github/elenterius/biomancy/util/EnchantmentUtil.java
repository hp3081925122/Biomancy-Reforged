package com.github.elenterius.biomancy.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class EnchantmentUtil {

	private EnchantmentUtil() {}

	public static Optional<Holder.Reference<Enchantment>> getHolder(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
		return registries.lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(key));
	}

	public static int getLevel(HolderLookup.Provider registries, ResourceKey<Enchantment> key, ItemStack stack) {
		return getHolder(registries, key).map(stack::getEnchantmentLevel).orElse(0);
	}

	public static int getLevel(ItemStack stack, ResourceKey<Enchantment> key) {
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getTagEnchantments().entrySet()) {
			if (entry.getKey().is(key)) return stack.getEnchantmentLevel(entry.getKey());
		}
		return 0;
	}

	public static int getMaxLevel(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
		return getHolder(registries, key).map(holder -> holder.value().getMaxLevel()).orElse(1);
	}

	public static boolean isDurabilityEnchantment(Holder<Enchantment> enchantment) {
		return enchantment.value().getSupportedItems().unwrapKey().filter(tagKey -> tagKey.equals(ItemTags.DURABILITY_ENCHANTABLE)).isPresent();
	}

	public static List<Map.Entry<EquipmentSlot, ItemStack>> getItemsWithEnchantment(Holder<Enchantment> enchantment, LivingEntity livingEntity, Predicate<ItemStack> predicate) {
		Map<EquipmentSlot, ItemStack> map = enchantment.value().getSlotItems(livingEntity);
		List<Map.Entry<EquipmentSlot, ItemStack>> list = new ArrayList<>();
		for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
			ItemStack stack = entry.getValue();
			if (!stack.isEmpty() && stack.getEnchantmentLevel(enchantment) > 0 && predicate.test(stack)) {
				list.add(entry);
			}
		}
		return list;
	}

}
