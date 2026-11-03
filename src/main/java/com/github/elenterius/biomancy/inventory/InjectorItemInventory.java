package com.github.elenterius.biomancy.inventory;

import com.github.elenterius.biomancy.api.serum.SerumContainer;
import com.github.elenterius.biomancy.component.InjectorContents;
import com.github.elenterius.biomancy.init.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class InjectorItemInventory {

	private final ItemStack cachedInventoryHost;
	private final LargeSingleItemStackHandler itemHandler;
	private final LazyOptional<IItemHandler> optionalItemHandler;

	private InjectorItemInventory(short maxSlotSize, ItemStack inventoryHost) {
		itemHandler = new LargeSingleItemStackHandler(maxSlotSize) {

			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() instanceof SerumContainer;
			}

			@Override
			protected void onContentsChanged() {
				serializeToHost();
			}
		};
		optionalItemHandler = LazyOptional.of(() -> itemHandler);
		cachedInventoryHost = inventoryHost;
	}

	public static InjectorItemInventory create(short maxSlotSize, ItemStack inventoryHost) {
		InjectorItemInventory inventory = new InjectorItemInventory(maxSlotSize, inventoryHost);
		inventory.deserializeFromHost();
		return inventory;
	}

	private void serializeToHost() {
		cachedInventoryHost.set(ModDataComponents.INJECTOR_CONTENTS, new InjectorContents(itemHandler.getStack(), (short) itemHandler.getAmount()));
	}

	private void deserializeFromHost() {
		InjectorContents contents = cachedInventoryHost.getOrDefault(ModDataComponents.INJECTOR_CONTENTS, InjectorContents.EMPTY);
		itemHandler.loadContents(contents.copyStack());
	}

	public boolean stillValid() {
		return !cachedInventoryHost.isEmpty();
	}

	public LargeSingleItemStackHandler getItemHandler() {
		deserializeFromHost();
		return itemHandler;
	}

	public LazyOptional<IItemHandler> getLazyOptional() {
		deserializeFromHost();
		return optionalItemHandler;
	}

}
