package com.github.elenterius.biomancy.util;

import com.github.elenterius.biomancy.item.weapon.ClawsItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class ItemCategoryUtil {

	private ItemCategoryUtil() {}

	public static boolean isHumanoidArmor(ItemStack stack) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
	}

	public static boolean isTieredTool(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (stack.getItem() instanceof ClawsItem) return true;
		return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
	}

}
