package com.github.elenterius.biomancy.enchantment;

import com.github.elenterius.biomancy.api.livingtool.LivingTool;
import com.github.elenterius.biomancy.api.nutrients.Nutrients;
import com.github.elenterius.biomancy.api.nutrients.NutrientsContainerItem;
import com.github.elenterius.biomancy.init.ModEnchantments;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SelfFeeding {

	private SelfFeeding() {}

	public static void repairLivingItems(Player player) {
		Optional<? extends Holder<Enchantment>> enchantment = EnchantmentUtil.getHolder(player.registryAccess(), ModEnchantments.SELF_FEEDING);
		if (enchantment.isEmpty()) return;

		List<Map.Entry<EquipmentSlot, ItemStack>> enchantedItems = EnchantmentUtil.getItemsWithEnchantment(enchantment.get(), player, LivingTool.NEED_NUTRIENTS_PREDICATE);
		if (enchantedItems.isEmpty()) return;

		Map.Entry<EquipmentSlot, ItemStack> slotItem = enchantedItems.get(player.getRandom().nextInt(enchantedItems.size()));
		ItemStack stack = slotItem.getValue();
		NutrientsContainerItem nutrientsContainer = (NutrientsContainerItem) stack.getItem();

		int neededRepairValue = nutrientsContainer.getMaxNutrients(stack) - nutrientsContainer.getNutrients(stack);
		ItemStack repairItemStack = getBestRepairItem(player, neededRepairValue);
		if (repairItemStack.isEmpty()) return;

		nutrientsContainer.increaseNutrients(stack, Nutrients.getRepairValue(repairItemStack));

		if (!player.getAbilities().instabuild) {
			if (repairItemStack.getCraftingRemainder() != null) {
				ItemStack craftingRemainder = repairItemStack.getCraftingRemainder().create();
				repairItemStack.shrink(1);
				if (!craftingRemainder.isEmpty() && !player.addItem(craftingRemainder)) {
					player.drop(craftingRemainder, false);
				}
			}
			else repairItemStack.shrink(1);
		}
	}

	static ItemStack getBestRepairItem(Player player, int neededRepairValue) {
		return getBestRepairItem(player, neededRepairValue, 0);
	}

	static ItemStack getBestRepairItem(Player player, int neededRepairValue, int maxError) {
		NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();

		ItemStack repairItemStack = ItemStack.EMPTY;
		int minError = Math.max(maxError, 0) + 1;

		for (int i = 0; i < 9; i++) {
			ItemStack itemStack = items.get(i);
			int repairValue = Nutrients.getRepairValue(itemStack);
			if (repairValue <= 0) continue;

			int error;
			Item item = itemStack.getItem();

			error = repairValue > neededRepairValue ? repairValue - neededRepairValue : -repairValue;

			if (error < minError) {
				minError = error;
				repairItemStack = itemStack;
			}
			else if (error == minError && (item == ModItems.NUTRIENT_PASTE.get() || item == ModItems.NUTRIENT_BAR.get())) {
				repairItemStack = itemStack;
			}
		}

		return repairItemStack;
	}

}
