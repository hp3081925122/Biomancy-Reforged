package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.tags.ModItemTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ModEnchantments {

	public static final ResourceKey<Enchantment> DESPOIL = create("despoil");
	public static final ResourceKey<Enchantment> ANESTHETIC = create("anesthetic");
	public static final ResourceKey<Enchantment> SURGICAL_PRECISION = create("surgical_precision");
	public static final ResourceKey<Enchantment> PARASITIC_METABOLISM = create("parasitic_metabolism");
	public static final ResourceKey<Enchantment> SELF_FEEDING = create("self_feeding");

	private static final int RARE_WEIGHT = 2;
	private static final int RARE_ANVIL_COST = 4;

	private ModEnchantments() {}

	private static ResourceKey<Enchantment> create(String name) {
		return ResourceKey.create(Registries.ENCHANTMENT, BiomancyMod.rl(name));
	}

	public static void bootstrap(BootstrapContext<Enchantment> context) {
		HolderGetter<Item> items = context.lookup(Registries.ITEM);
		HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);

		register(context, DESPOIL, Enchantment.enchantment(
				Enchantment.definition(
						items.getOrThrow(ModItemTags.ENCHANTABLE_WEAPON),
						RARE_WEIGHT,
						3,
						Enchantment.dynamicCost(15, 9),
						Enchantment.dynamicCost(65, 9),
						RARE_ANVIL_COST,
						EquipmentSlotGroup.HAND
				)
		).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(Enchantments.SILK_TOUCH))));

		register(context, ANESTHETIC, Enchantment.enchantment(defaultDefinition(items, ModItemTags.ENCHANTABLE_SYRINGE, 1, EquipmentSlotGroup.MAINHAND)));
		register(context, SURGICAL_PRECISION, Enchantment.enchantment(defaultDefinition(items, ModItemTags.ENCHANTABLE_SURGERY, 3, EquipmentSlotGroup.MAINHAND)));
		register(context, PARASITIC_METABOLISM, Enchantment.enchantment(defaultDefinition(items, ModItemTags.ENCHANTABLE_LIVING, 1, EquipmentSlotGroup.ANY)));
		register(context, SELF_FEEDING, Enchantment.enchantment(defaultDefinition(items, ModItemTags.ENCHANTABLE_LIVING, 1, EquipmentSlotGroup.ANY)));
	}

	private static Enchantment.EnchantmentDefinition defaultDefinition(HolderGetter<Item> items, TagKey<Item> supportedItems, int maxLevel, EquipmentSlotGroup slots) {
		return Enchantment.definition(
				items.getOrThrow(supportedItems),
				RARE_WEIGHT,
				maxLevel,
				Enchantment.dynamicCost(11, 10),
				Enchantment.dynamicCost(16, 10),
				RARE_ANVIL_COST,
				slots
		);
	}

	private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
		context.register(key, builder.build(key.identifier()));
	}

}
