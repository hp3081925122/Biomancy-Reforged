package com.github.elenterius.biomancy.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraftforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ItemStackInventory {

	private final ItemStack cachedInventoryHost;

	private final HostItemStackHandler itemHandler;
	private final LazyOptional<IItemHandler> optionalItemHandler;

	ItemStackInventory(int slots, int maxSlotSize, ItemStack inventoryHost) {
		itemHandler = new HostItemStackHandler(slots, maxSlotSize);
		optionalItemHandler = LazyOptional.of(() -> itemHandler);
		cachedInventoryHost = inventoryHost;
	}

	public static ItemStackInventory create(int slots, int maxSlotSize, ItemStack inventoryHost) {
		ItemStackInventory inventory = new ItemStackInventory(slots, maxSlotSize, inventoryHost);
		inventory.deserializeFromHost();
		return inventory;
	}

	private void serializeToHost() {
		cachedInventoryHost.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(itemHandler.getStacks()));
	}

	private void deserializeFromHost() {
		ItemContainerContents contents = cachedInventoryHost.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		NonNullList<ItemStack> stacks = NonNullList.withSize(itemHandler.getSlots(), ItemStack.EMPTY);
		contents.copyInto(stacks);
		itemHandler.loadStacks(stacks);
	}

	public boolean stillValid(Player player) {
		return !cachedInventoryHost.isEmpty();
	}

	public IItemHandler getItemHandler() {
		deserializeFromHost();
		return itemHandler;
	}

	public LazyOptional<IItemHandler> getLazyOptional() {
		deserializeFromHost();
		return optionalItemHandler;
	}

	private final class HostItemStackHandler extends ItemStackHandler {

		private final int maxSlotSize;

		HostItemStackHandler(int slots, int maxSlotSize) {
			super(slots);
			this.maxSlotSize = maxSlotSize;
		}

		@Override
		public int getSlotLimit(int slot) {
			return maxSlotSize;
		}

		@Override
		protected void onContentsChanged(int slot) {
			serializeToHost();
		}

		NonNullList<ItemStack> getStacks() {
			return stacks;
		}

		void loadStacks(NonNullList<ItemStack> newStacks) {
			for (int i = 0; i < stacks.size(); i++) {
				stacks.set(i, newStacks.get(i));
			}
		}

	}

}
