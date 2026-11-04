package com.github.elenterius.biomancy.mixin;

import com.github.elenterius.biomancy.item.ConditionalEnchantable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
	private void biomancy$onIsEnchantable(CallbackInfoReturnable<Boolean> cir) {
		ItemStack self = (ItemStack) (Object) this;
		if (self.getItem() instanceof ConditionalEnchantable enchantable && !enchantable.isEnchantable(self)) {
			cir.setReturnValue(false);
		}
	}

}
