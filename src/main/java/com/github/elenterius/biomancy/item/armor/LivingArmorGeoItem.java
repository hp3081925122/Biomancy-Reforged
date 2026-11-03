package com.github.elenterius.biomancy.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class LivingArmorGeoItem extends LivingArmorItem implements GeoItem {

	protected LivingArmorGeoItem(Holder<ArmorMaterial> material, Type type, int maxNutrients, Properties properties) {
		super(material, type, maxNutrients, properties);
	}

	@Override
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		return MissingTextureAtlasSprite.getLocation();
	}

}
