package com.github.elenterius.biomancy.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class LivingArmorGeoItem extends LivingArmorItem implements GeoItem {

	// 使用原版缺省贴图定位常量，避免引用仅客户端类 MissingTextureAtlasSprite
	private static final ResourceLocation MISSING_TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("missingno");

	protected LivingArmorGeoItem(Holder<ArmorMaterial> material, Type type, int maxNutrients, Properties properties) {
		super(material, type, maxNutrients, properties);
	}

	@Override
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		return MISSING_TEXTURE_LOCATION;
	}

}
