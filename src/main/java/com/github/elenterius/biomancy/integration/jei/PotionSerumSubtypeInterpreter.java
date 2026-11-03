package com.github.elenterius.biomancy.integration.jei;

import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.item.PotionSerumItem;
import com.github.elenterius.biomancy.serum.PotionSerum;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import org.jspecify.annotations.Nullable;

public class PotionSerumSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

	public static final PotionSerumSubtypeInterpreter INSTANCE = new PotionSerumSubtypeInterpreter();

	private PotionSerumSubtypeInterpreter() {}

	private static @Nullable ResourceKey<Potion> getPotionKey(ItemStack stack) {
		if (!stack.has(ModDataComponents.SERUM_DATA)) return null;
		if (!(stack.getItem() instanceof PotionSerumItem container)) return null;

		Holder<Potion> potion = PotionSerum.getPotion(container.getSerumData(stack));
		return potion != null ? potion.unwrapKey().orElse(null) : null;
	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
		return getPotionKey(stack);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
		ResourceKey<Potion> key = getPotionKey(stack);
		return key != null ? key.location().toString() : "";
	}

}
