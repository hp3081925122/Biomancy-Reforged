package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {

	public static final ResourceKey<Enchantment> DESPOIL = create("despoil");
	public static final ResourceKey<Enchantment> ANESTHETIC = create("anesthetic");
	public static final ResourceKey<Enchantment> SURGICAL_PRECISION = create("surgical_precision");
	public static final ResourceKey<Enchantment> PARASITIC_METABOLISM = create("parasitic_metabolism");
	public static final ResourceKey<Enchantment> SELF_FEEDING = create("self_feeding");

	private ModEnchantments() {}

	private static ResourceKey<Enchantment> create(String name) {
		return ResourceKey.create(Registries.ENCHANTMENT, BiomancyMod.rl(name));
	}

}
