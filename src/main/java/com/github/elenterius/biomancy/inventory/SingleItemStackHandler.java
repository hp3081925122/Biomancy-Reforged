package com.github.elenterius.biomancy.inventory;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SingleItemStackHandler implements SerializableItemHandler {

	public static final String ITEM_TAG = "Item";

	protected ItemStack cachedStack = ItemStack.EMPTY;

	public SingleItemStackHandler() {}

	protected void validateSlotIndex(int slot) {
		if (slot != 0) throw new IllegalArgumentException("Slot " + slot + " not valid - must be 0");
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public int getSlotLimit(int slot) {
		return cachedStack.getMaxStackSize();
	}

	public boolean isItemValid(ItemStack stack) {
		return true;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot == 0 && isItemValid(stack);
	}

	public boolean isEmpty() {
		return cachedStack.isEmpty();
	}

	public boolean isFull() {
		return !isEmpty() && getAmount() >= getMaxAmount();
	}

	public int getAmount() {
		return cachedStack.getCount();
	}

	public void setAmount(short amount) {
		if (!cachedStack.isEmpty()) {
			cachedStack.setCount(Mth.clamp(amount, 0, getMaxAmount()));
			onContentsChanged();
		}
	}

	public int getMaxAmount() {
		return getSlotLimit(0);
	}

	public Item getItem() {
		return cachedStack.getItem();
	}

	public ItemStack getStack() {
		return cachedStack;
	}

	public void setStack(ItemStack stack) {
		cachedStack = stack;
		onContentsChanged();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return cachedStack;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		validateSlotIndex(slot);
		setStack(stack);
	}

	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		return insertItem(0, stack, simulate);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stackIn, boolean simulate) {
		ItemStack remainder = internalInsertItem(slot, stackIn, simulate);
		if (!simulate) {
			onContentsChanged();
		}
		return remainder;
	}

	protected ItemStack internalInsertItem(int slot, ItemStack stackIn, boolean simulate) {
		validateSlotIndex(slot);
		if (stackIn.isEmpty()) return ItemStack.EMPTY;
		if (!isItemValid(slot, stackIn)) return stackIn;

		if (!cachedStack.isEmpty() && !ItemHandlerUtil.canItemStacksStack(stackIn, cachedStack)) return stackIn;
		if (getAmount() >= getMaxAmount()) return stackIn;

		int insertGoal = stackIn.getCount();
		int newAmount = getAmount() + insertGoal;
		int overflow = newAmount > getMaxAmount() ? newAmount - getMaxAmount() : 0;

		if (!simulate) {
			int insertAmount = overflow > 0 ? insertGoal - overflow : insertGoal;
			if (cachedStack.isEmpty()) cachedStack = stackIn.copyWithCount(insertAmount);
			else cachedStack.grow(insertAmount);
		}

		return overflow > 0 ? stackIn.copyWithCount(overflow) : ItemStack.EMPTY;
	}

	public ItemStack extractItem(int amount, boolean simulate) {
		return extractItem(0, amount, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack remainder = internalExtractItem(slot, amount, simulate);
		if (!simulate) {
			onContentsChanged();
		}
		return remainder;
	}

	protected ItemStack internalExtractItem(int slot, int amount, boolean simulate) {
		validateSlotIndex(slot);
		if (amount == 0) return ItemStack.EMPTY;

		if (cachedStack.isEmpty()) return ItemStack.EMPTY;
		int extractGoal = Math.min(amount, getMaxAmount());

		if (getAmount() <= extractGoal) {
			if (!simulate) {
				ItemStack stack = cachedStack;
				cachedStack = ItemStack.EMPTY;
				return stack;
			}
			else return cachedStack.copy();
		}
		else {
			if (!simulate) {
				cachedStack.grow(-extractGoal);
			}
			return cachedStack.copyWithCount(extractGoal);
		}
	}

	public void serializeItemAmount(ValueOutput output) {}

	public int deserializeItemAmount(ValueInput input) {
		return -1;
	}

	@Override
	public void serialize(ValueOutput output) {
		serializeItemAmount(output);
		if (!cachedStack.isEmpty()) {
			output.store(ITEM_TAG, ItemStack.CODEC, cachedStack.copyWithCount(Math.min(cachedStack.getCount(), 64)));
		}
	}

	@Override
	public void deserialize(ValueInput input) {
		cachedStack = input.read(ITEM_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);

		int itemAmount = deserializeItemAmount(input);
		if (itemAmount > cachedStack.getCount()) {
			cachedStack.setCount(itemAmount);
		}
	}

	protected void onContentsChanged() {
	}

}
