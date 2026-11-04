package com.github.elenterius.biomancy.item.armor;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import com.geckolib.animatable.GeoItem;

public abstract class LivingArmorGeoItem extends LivingArmorItem implements GeoItem {

	protected LivingArmorGeoItem(ArmorMaterial material, ArmorType type, int maxNutrients, Properties properties) {
		super(material, type, maxNutrients, properties);
	}

}
