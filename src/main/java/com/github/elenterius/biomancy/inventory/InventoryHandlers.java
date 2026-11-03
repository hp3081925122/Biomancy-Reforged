package com.github.elenterius.biomancy.inventory;

import com.github.elenterius.biomancy.api.nutrients.Nutrients;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class InventoryHandlers {

	public static final Predicate<ItemStack> EMPTY_ITEM_INVENTORY_PREDICATE = stack -> {

		if (stack.getItem() instanceof BundleItem) return false;
		if (!stack.getItem().canFitInsideContainerItems()) return false;

		IItemHandler itemHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);
		if (itemHandler == null) return true;

		int slots = itemHandler.getSlots();
		if (slots > 200) return false;

		for (int i = 0; i < slots; i++) {
			if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	};

	private InventoryHandlers() {}

	public static InventoryHandler<FixedSizeItemStackHandler> standard(int slotAmount) {
		return new InventoryHandler<>(new FixedSizeItemStackHandler(slotAmount));
	}

	public static InventoryHandler<FixedSizeItemStackHandler> standard(int slotAmount, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return new InventoryHandler<>(handler);
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.DenyInput> denyInput(T itemStackHandler) {
		return new InventoryHandler<>(new BehavioralItemHandler.DenyInput(itemStackHandler));
	}

	public static InventoryHandler<BehavioralItemHandler.DenyInput> denyInput(int slotAmount, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return new InventoryHandler<>(new BehavioralItemHandler.DenyInput(handler));
	}

	public static InventoryHandler<BehavioralItemHandler.PredicateFilterInput> filterInput(int slotAmount, List<Predicate<ItemStack>> slotFilters) {
		return filterInput(new FixedSizeItemStackHandler(slotAmount), slotFilters);
	}

	public static InventoryHandler<BehavioralItemHandler.PredicateFilterInput> filterInput(int slotAmount, List<Predicate<ItemStack>> slotFilters, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return filterInput(handler, slotFilters);
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.PredicateFilterInput> filterInput(T itemStackHandler, List<Predicate<ItemStack>> slotFilters) {
		return new InventoryHandler<>(new BehavioralItemHandler.PredicateFilterInput(itemStackHandler, slotFilters));
	}

	public static InventoryHandler<BehavioralItemHandler.ItemStackFilterInput> filterInput(int slotAmount) {
		return filterInput(new FixedSizeItemStackHandler(slotAmount));
	}

	public static InventoryHandler<BehavioralItemHandler.ItemStackFilterInput> filterInput(int slotAmount, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return filterInput(handler);
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.ItemStackFilterInput> filterInput(T itemStackHandler) {
		return new InventoryHandler<>(new BehavioralItemHandler.ItemStackFilterInput(itemStackHandler));
	}

	public static InventoryHandler<BehavioralItemHandler.LockableItemStackFilterInput> lockableFilterInput(int slotAmount, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return lockableFilterInput(handler);
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.LockableItemStackFilterInput> lockableFilterInput(T itemStackHandler) {
		return new InventoryHandler<>(new BehavioralItemHandler.LockableItemStackFilterInput(itemStackHandler));
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.PredicateFilterInput> denyItemWithFilledInventory(T itemHandler) {
		return new InventoryHandler<>(new BehavioralItemHandler.PredicateFilterInput(itemHandler, IntStream.range(0, itemHandler.getSlots()).mapToObj(x -> EMPTY_ITEM_INVENTORY_PREDICATE).toList()));
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.PredicateFilterInput> denyItemWithFilledInventory(int slotAmount, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return denyItemWithFilledInventory(handler);
	}

	public static InventoryHandler<BehavioralItemHandler.PredicateFilterInput> filterFuel(int slotAmount) {
		return filterFuel(new FixedSizeItemStackHandler(slotAmount));
	}

	public static InventoryHandler<BehavioralItemHandler.PredicateFilterInput> filterFuel(int slotAmount, Notify onInventoryChanged) {
		FixedSizeItemStackHandler handler = new FixedSizeItemStackHandler(slotAmount) {
			@Override
			protected void onContentsChanged(int slot) {
				onInventoryChanged.invoke();
			}
		};
		return filterFuel(handler);
	}

	public static <T extends SerializableItemHandler> InventoryHandler<BehavioralItemHandler.PredicateFilterInput> filterFuel(T itemHandler) {
		return filterInput(itemHandler, IntStream.range(0, itemHandler.getSlots()).mapToObj(x -> Nutrients.FUEL_PREDICATE).toList());
	}

}
