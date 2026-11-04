package com.github.elenterius.biomancy.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class ItemStackFilter implements Predicate<ItemStack> {

	public static final String FILTER_KEY = "Filter";
	public static final String STRICT_KEY = "Strict";

	public static final Codec<ItemStackFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.OPTIONAL_CODEC.optionalFieldOf(FILTER_KEY).forGetter(filter -> Optional.ofNullable(filter.filter)),
			Codec.BOOL.optionalFieldOf(STRICT_KEY, false).forGetter(filter -> filter.isStrict)
	).apply(instance, (filter, strict) -> new ItemStackFilter(filter.orElse(null), strict)));

	public static final ItemStackFilter ALLOW_ANY = new ItemStackFilter(null, false);
	public static final ItemStackFilter ALLOW_NONE = new ItemStackFilter(ItemStack.EMPTY, false);

	@Nullable
	private ItemStack filter;
	private boolean isStrict;

	protected ItemStackFilter(@Nullable ItemStack filter, boolean isStrict) {
		this.filter = filter;
		this.isStrict = isStrict;
	}

	public static ItemStackFilter of(Item filter) {
		return of(filter.getDefaultInstance(), false);
	}

	public static ItemStackFilter of(@Nullable ItemStack filter) {
		return of(filter, true);
	}

	private static ItemStackFilter of(@Nullable ItemStack filter, boolean isStrict) {
		if (filter == null) return ALLOW_ANY;
		if (filter.isEmpty()) return ALLOW_NONE;

		filter = new ItemStack(filter.typeHolder(), 1, filter.getComponentsPatch().forget(type -> type == DataComponents.ENCHANTMENTS || type == DataComponents.ATTRIBUTE_MODIFIERS));

		return new ItemStackFilter(filter, isStrict);
	}

	@Override
	public boolean test(ItemStack stack) {
		if (stack.isEmpty()) return false;

		if (filter == null) return true;
		if (filter.isEmpty()) return false;

		if (isStrict) {
			return ItemStack.isSameItemSameComponents(filter, stack);
		}
		else {
			return filter.is(stack.getItem());
		}
	}

	public boolean allowsAny() {
		return filter == null;
	}

	public @Nullable ItemStack getItemStack() {
		return filter;
	}

}
