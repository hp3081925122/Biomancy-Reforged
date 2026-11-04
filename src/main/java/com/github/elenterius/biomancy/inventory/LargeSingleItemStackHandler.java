package com.github.elenterius.biomancy.inventory;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class LargeSingleItemStackHandler extends SingleItemStackHandler {

	public static final String ITEM_AMOUNT_TAG = "ItemAmount";

	private final short maxItemAmount;
	private short itemAmount;

	public LargeSingleItemStackHandler() {
		this(Short.MAX_VALUE);
	}

	public LargeSingleItemStackHandler(short maxItemAmount) {
		this.maxItemAmount = maxItemAmount;
	}

	@Override
	public int getSlotLimit(int slot) {
		return maxItemAmount;
	}

	@Override
	public int getMaxAmount() {
		return maxItemAmount;
	}

	@Override
	public int getAmount() {
		return itemAmount;
	}

	@Override
	public void setAmount(short amount) {
		if (!cachedStack.isEmpty()) {
			int value = Mth.clamp(amount, 0, getMaxAmount());
			itemAmount = (short) value;
			cachedStack.setCount(value);
			onContentsChanged();
		}
	}

	@Override
	public void setStack(ItemStack stack) {
		cachedStack = stack;
		itemAmount = (short) cachedStack.getCount();
		onContentsChanged();
	}

	public void loadContents(ItemStack stack) {
		cachedStack = stack;
		itemAmount = (short) cachedStack.getCount();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stackIn, boolean simulate) {
		ItemStack remainder = internalInsertItem(slot, stackIn, simulate);
		if (!simulate) {
			itemAmount = (short) cachedStack.getCount();
			onContentsChanged();
		}
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack remainder = internalExtractItem(slot, amount, simulate);
		if (!simulate) {
			itemAmount = (short) cachedStack.getCount();
			onContentsChanged();
		}
		return remainder;
	}

	@Override
	public void serializeItemAmount(ValueOutput output) {
		output.putShort(ITEM_AMOUNT_TAG, itemAmount);
	}

	@Override
	public int deserializeItemAmount(ValueInput input) {
		itemAmount = (short) input.getShortOr(ITEM_AMOUNT_TAG, (short) 0);
		return itemAmount;
	}

	@Override
	public void serialize(ValueOutput output) {
		if (!cachedStack.isEmpty()) {
			serializeItemAmount(output);
			output.store(ITEM_TAG, ItemStack.CODEC, cachedStack.copyWithCount(Math.min(itemAmount, cachedStack.getMaxStackSize())));
		}
	}

	@Override
	public void deserialize(ValueInput input) {
		cachedStack = input.read(ITEM_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		if (!cachedStack.isEmpty()) {
			cachedStack.setCount(deserializeItemAmount(input));
		}
	}

}
