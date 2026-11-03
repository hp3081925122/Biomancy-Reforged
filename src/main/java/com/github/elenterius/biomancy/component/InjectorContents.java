package com.github.elenterius.biomancy.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public final class InjectorContents {

	public static final InjectorContents EMPTY = new InjectorContents(ItemStack.EMPTY, (short) 0);

	public static final Codec<InjectorContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(contents -> contents.stack),
			Codec.SHORT.fieldOf("amount").forGetter(contents -> contents.amount)
	).apply(instance, InjectorContents::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, InjectorContents> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_STREAM_CODEC, contents -> contents.stack,
			ByteBufCodecs.SHORT, contents -> contents.amount,
			InjectorContents::new
	);

	private final ItemStack stack;
	private final short amount;

	public InjectorContents(ItemStack stack, short amount) {
		this.stack = stack.copyWithCount(1);
		this.amount = stack.isEmpty() ? 0 : amount;
	}

	public ItemStack copyStack() {
		return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(amount);
	}

	public short amount() {
		return amount;
	}

	public boolean isEmpty() {
		return stack.isEmpty() || amount <= 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof InjectorContents that)) return false;
		return amount == that.amount && ItemStack.isSameItemSameComponents(stack, that.stack);
	}

	@Override
	public int hashCode() {
		return 31 * ItemStack.hashItemAndComponents(stack) + amount;
	}

}
