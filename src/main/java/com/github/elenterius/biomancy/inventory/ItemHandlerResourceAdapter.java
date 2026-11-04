package com.github.elenterius.biomancy.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"deprecation", "removal"})
public class ItemHandlerResourceAdapter implements ResourceHandler<ItemResource> {

	private final IItemHandlerModifiable itemHandler;
	private final SnapshotJournal<List<ItemStack>> journal = new SnapshotJournal<>() {
		@Override
		protected List<ItemStack> createSnapshot() {
			List<ItemStack> snapshot = new ArrayList<>(itemHandler.getSlots());
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				snapshot.add(itemHandler.getStackInSlot(i).copy());
			}
			return snapshot;
		}

		@Override
		protected void revertToSnapshot(List<ItemStack> snapshot) {
			for (int i = 0; i < snapshot.size(); i++) {
				itemHandler.setStackInSlot(i, snapshot.get(i).copy());
			}
		}
	};

	public ItemHandlerResourceAdapter(IItemHandlerModifiable itemHandler) {
		this.itemHandler = itemHandler;
	}

	public IItemHandlerModifiable getItemHandler() {
		return itemHandler;
	}

	@Override
	public int size() {
		return itemHandler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(itemHandler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return itemHandler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		int slotLimit = itemHandler.getSlotLimit(index);
		if (resource.isEmpty()) return slotLimit;
		return Math.min(slotLimit, resource.getMaxStackSize());
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return itemHandler.isItemValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		if (amount == 0) return 0;

		ItemStack remainder = itemHandler.insertItem(index, resource.toStack(amount), true);
		int insertable = amount - remainder.getCount();
		if (insertable <= 0) return 0;

		journal.updateSnapshots(transaction);
		ItemStack actualRemainder = itemHandler.insertItem(index, resource.toStack(insertable), false);
		return insertable - actualRemainder.getCount();
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		if (amount == 0) return 0;
		if (!resource.matches(itemHandler.getStackInSlot(index))) return 0;

		ItemStack simulated = itemHandler.extractItem(index, amount, true);
		if (simulated.isEmpty() || !resource.matches(simulated)) return 0;

		journal.updateSnapshots(transaction);
		return itemHandler.extractItem(index, simulated.getCount(), false).getCount();
	}

}
