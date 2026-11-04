package com.github.elenterius.biomancy.init;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.init.tags.ModItemTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

public final class ModArmorMaterials {

	private static final int ACOLYTE_DURABILITY_MULTIPLIER = 1;
	private static final int WARRIOR_DURABILITY_MULTIPLIER = 1;

	public static final ArmorMaterial ACOLYTE = new ArmorMaterial(
			ACOLYTE_DURABILITY_MULTIPLIER,
			defense(2, 6, 5, 2),
			0,
			ModSoundEvents.ARMOR_EQUIP_BIO_ALCHEMIST,
			0.25f,
			0.1f,
			ModItemTags.NO_REPAIR_MATERIALS,
			assetKey("acolyte")
	);

	public static final ArmorMaterial WARRIOR = new ArmorMaterial(
			WARRIOR_DURABILITY_MULTIPLIER,
			defense(3, 8, 6, 3),
			0,
			ModSoundEvents.ARMOR_EQUIP_WARRIOR,
			0.5f,
			0,
			ModItemTags.NO_REPAIR_MATERIALS,
			assetKey("warrior")
	);

	private ModArmorMaterials() {}

	public static Item.Properties applyArmorProperties(ArmorMaterial material, ArmorType type, Item.Properties properties) {
		properties = properties.durability(type.getDurability(material.durability()))
				.component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()).setEquipSound(material.equipSound()).setAsset(material.assetId()).build())
				.repairable(material.repairIngredient());
		if (material.enchantmentValue() > 0) {
			properties = properties.enchantable(material.enchantmentValue());
		}
		return properties;
	}

	private static ResourceKey<EquipmentAsset> assetKey(String name) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, BiomancyMod.rl(name));
	}

	private static Map<ArmorType, Integer> defense(int helmet, int chestplate, int leggings, int boots) {
		return Util.make(new EnumMap<>(ArmorType.class), map -> {
			map.put(ArmorType.HELMET, helmet);
			map.put(ArmorType.CHESTPLATE, chestplate);
			map.put(ArmorType.LEGGINGS, leggings);
			map.put(ArmorType.BOOTS, boots);
		});
	}

}
