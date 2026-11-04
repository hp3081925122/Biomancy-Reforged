package com.github.elenterius.biomancy.item;

import net.minecraft.world.item.ItemStack;

public interface ConditionalEnchantable {

	boolean isEnchantable(ItemStack stack);

}
