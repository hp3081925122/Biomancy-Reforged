package com.github.elenterius.biomancy.util.animation;

import com.geckolib.constant.dataticket.DataTicket;
import net.minecraft.world.item.ItemStack;

public final class ModDataTickets {

	public static final DataTicket<ItemStack> ITEMSTACK = DataTicket.create("biomancy:itemstack", ItemStack.class);

	private ModDataTickets() {}

}
