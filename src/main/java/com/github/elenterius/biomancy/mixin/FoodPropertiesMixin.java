package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.init.ModMobEffects;
import com.github.elenterius.biomancy.init.tags.ModItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin {

	@Shadow
	public abstract int nutrition();

	@Inject(method = "onConsume", at = @At("TAIL"))
	private void onConsumeFood(Level level, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo ci) {
		if (level.isClientSide()) return;
		if (nutrition() > 2 && stack.is(ItemTags.MEAT) && stack.is(ModItemTags.FRESH_RAW_MEATS) && user.getRandom().nextFloat() < 0.2f) {
			user.addEffect(new MobEffectInstance(ModMobEffects.PRIMORDIAL_INFESTATION, 20 * 8, 0));
		}
	}

}
