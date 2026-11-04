package com.github.elenterius.biomancy.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface CrosshairProvider {

	int TEXTURE_SIZE = 31;

	Identifier getCrosshairTexture(ItemStack stack, Player player);

}
