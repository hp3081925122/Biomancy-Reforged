package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.item.armor.LivingArmorItem;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Avatar.class)
public abstract class AvatarMixin extends LivingEntity {

	protected AvatarMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "isModelPartShown", at = @At("HEAD"), cancellable = true)
	private void onIsModelPartShown(PlayerModelPart part, CallbackInfoReturnable<Boolean> cir) {
		switch (part) {
			case HAT -> {
				if (getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof LivingArmorItem armor && armor.getType() == ArmorType.HELMET) cir.setReturnValue(false);
			}
			case JACKET, LEFT_SLEEVE, RIGHT_SLEEVE -> {
				if (getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof LivingArmorItem armor && armor.getType() == ArmorType.CHESTPLATE) cir.setReturnValue(false);
			}
			case LEFT_PANTS_LEG, RIGHT_PANTS_LEG -> {
				if (getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof LivingArmorItem armor && armor.getType() == ArmorType.LEGGINGS) cir.setReturnValue(false);
			}
			case CAPE -> {}
		}
	}

}
