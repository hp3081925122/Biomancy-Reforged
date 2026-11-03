package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModArmorMaterials {

	public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, BiomancyMod.MOD_ID);

	private static final int ACOLYTE_DURABILITY_MULTIPLIER = 1;
	private static final int WARRIOR_DURABILITY_MULTIPLIER = 1;

	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ACOLYTE = ARMOR_MATERIALS.register("acolyte", () -> new ArmorMaterial(
			defense(2, 6, 5, 2),
			0,
			ModSoundEvents.ARMOR_EQUIP_BIO_ALCHEMIST,
			() -> Ingredient.EMPTY,
			List.of(new ArmorMaterial.Layer(BiomancyMod.rl("acolyte"))),
			0.25f,
			0.1f
	));

	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WARRIOR = ARMOR_MATERIALS.register("warrior", () -> new ArmorMaterial(
			defense(3, 8, 6, 3),
			0,
			ModSoundEvents.ARMOR_EQUIP_WARRIOR,
			() -> Ingredient.EMPTY,
			List.of(new ArmorMaterial.Layer(BiomancyMod.rl("warrior"))),
			0.5f,
			0
	));

	private ModArmorMaterials() {}

	public static int getDurability(Holder<ArmorMaterial> material, ArmorItem.Type type) {
		int multiplier = material.is(WARRIOR.getKey()) ? WARRIOR_DURABILITY_MULTIPLIER : ACOLYTE_DURABILITY_MULTIPLIER;
		return type.getDurability(multiplier);
	}

	private static Map<ArmorItem.Type, Integer> defense(int helmet, int chestplate, int leggings, int boots) {
		return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
			map.put(ArmorItem.Type.HELMET, helmet);
			map.put(ArmorItem.Type.CHESTPLATE, chestplate);
			map.put(ArmorItem.Type.LEGGINGS, leggings);
			map.put(ArmorItem.Type.BOOTS, boots);
		});
	}

}
