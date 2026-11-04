package com.github.elenterius.biomancy.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class VariableOutput {

	private static final Codec<Item> RESULT_ITEM_CODEC = BuiltInRegistries.ITEM.byNameCodec()
			.validate(item -> item == Items.AIR ? DataResult.error(() -> "Result can't be Empty") : DataResult.success(item));

	public static final Codec<VariableOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESULT_ITEM_CODEC.fieldOf("item").forGetter(VariableOutput::getItem),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(o -> o.components),
			ItemCountRange.CODEC.fieldOf("countRange").forGetter(VariableOutput::getCountRange)
	).apply(instance, VariableOutput::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, VariableOutput> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, VariableOutput::getItemStack,
			ItemCountRange.STREAM_CODEC, VariableOutput::getCountRange,
			VariableOutput::new
	);

	private final Item item;
	private final DataComponentPatch components;
	private final ItemCountRange countRange;

	public VariableOutput(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public VariableOutput(ItemStack stack, int count) {
		this(stack.getItem(), stack.getComponentsPatch(), new ItemCountRange.ConstantValue(count));
	}

	public VariableOutput(ItemStack stack, int min, int max) {
		this(stack.getItem(), stack.getComponentsPatch(), new ItemCountRange.UniformRange(min, max));
	}

	public VariableOutput(ItemStack stack, int n, float p) {
		this(stack.getItem(), stack.getComponentsPatch(), new ItemCountRange.BinomialRange(n, p));
	}

	public VariableOutput(ItemLike item) {
		this(item, 1);
	}

	public VariableOutput(ItemLike item, int count) {
		this(item, new ItemCountRange.ConstantValue(count));
	}

	public VariableOutput(ItemLike item, int min, int max) {
		this(item, new ItemCountRange.UniformRange(min, max));
	}

	public VariableOutput(ItemLike item, int n, float p) {
		this(item, new ItemCountRange.BinomialRange(n, p));
	}

	public VariableOutput(ItemStack stack, ItemCountRange countRange) {
		this(stack.getItem(), stack.getComponentsPatch(), countRange);
	}

	public VariableOutput(ItemLike item, ItemCountRange countRange) {
		this(item, DataComponentPatch.EMPTY, countRange);
	}

	public VariableOutput(ItemLike item, DataComponentPatch components, ItemCountRange countRange) {
		this.item = item.asItem();
		this.components = components;
		this.countRange = countRange;
	}

	public Item getItem() {
		return item;
	}

	public DataComponentPatch getComponents() {
		return components;
	}

	public ItemStack getItemStack() {
		return new ItemStack(item.builtInRegistryHolder(), 1, components);
	}

	public ItemStack getItemStack(RandomSource rng) {
		int count = getCount(rng);
		if (count < 1) return ItemStack.EMPTY;

		return new ItemStack(item.builtInRegistryHolder(), count, components);
	}

	public int getCount(RandomSource rng) {
		return countRange.getCount(rng);
	}

	public ItemCountRange getCountRange() {
		return countRange;
	}

}
