package com.github.elenterius.biomancy.init;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.github.elenterius.biomancy.init.tags.ModItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public final class ModTiers {

	public static final ToolMaterial PRIMAL_FLESH = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 500, 4.5f, 1f, 14, ModItemTags.PRIMAL_FLESH_TOOL_MATERIALS);
	public static final ToolMaterial BONE = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 142, 4.5f, 1f, 7, ModItemTags.BONE_TOOL_MATERIALS);
	public static final ToolMaterial BIOFLESH = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9f, 4f, 15, ModItemTags.FRESH_RAW_MEATS);

	private ModTiers() {}

	public static Item.Properties applyCommonProperties(ToolMaterial material, Item.Properties properties) {
		return properties.durability(material.durability()).repairable(material.repairItems()).enchantable(material.enchantmentValue());
	}

	public static Item.Properties applySwordProperties(ToolMaterial material, Item.Properties properties) {
		return material.applySwordProperties(properties, 0, 0).attributes(ItemAttributeModifiers.EMPTY);
	}

	public static ItemAttributeModifiers createSwordAttributes(ToolMaterial material, float attackDamageModifier, float attackSpeedModifier) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamageModifier + material.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeedModifier, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.build();
	}

}
