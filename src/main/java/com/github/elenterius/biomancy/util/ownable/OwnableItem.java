package com.github.elenterius.biomancy.util.ownable;

import com.github.elenterius.biomancy.init.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public interface OwnableItem {

	default Optional<UUID> getOwner(ItemStack stack) {
		return Optional.ofNullable(stack.get(ModDataComponents.OWNER));
	}

	default void setOwner(ItemStack stack, UUID uuid) {
		stack.set(ModDataComponents.OWNER, uuid);
	}

	default void removeOwner(ItemStack stack) {
		stack.remove(ModDataComponents.OWNER);
	}

	default boolean hasOwner(ItemStack stack) {
		return stack.has(ModDataComponents.OWNER);
	}

	default boolean isOwner(ItemStack stack, UUID uuid) {
		return getOwner(stack).map(value -> value.equals(uuid)).orElse(false);
	}

}
