package com.github.elenterius.biomancy.inventory;

import com.github.elenterius.biomancy.api.serum.SerumContainer;
import com.github.elenterius.biomancy.component.InjectorContents;
import com.github.elenterius.biomancy.init.ModDataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class InjectorItemAccessHandler extends ItemAccessResourceHandler<ItemResource> {

	private final Item validItem;
	private final short maxSlotSize;

	public InjectorItemAccessHandler(ItemAccess itemAccess, short maxSlotSize) {
		super(itemAccess, 1);
		this.validItem = itemAccess.getResource().getItem();
		this.maxSlotSize = maxSlotSize;
	}

	private InjectorContents getContents(ItemResource accessResource) {
		return accessResource.getOrDefault(ModDataComponents.INJECTOR_CONTENTS.get(), InjectorContents.EMPTY);
	}

	@Override
	protected ItemResource getResourceFrom(ItemResource accessResource, int index) {
		if (!accessResource.is(validItem)) return ItemResource.EMPTY;
		InjectorContents contents = getContents(accessResource);
		return contents.isEmpty() ? ItemResource.EMPTY : ItemResource.of(contents.copyStack());
	}

	@Override
	protected int getAmountFrom(ItemResource accessResource, int index) {
		if (!accessResource.is(validItem)) return 0;
		InjectorContents contents = getContents(accessResource);
		return contents.isEmpty() ? 0 : contents.amount();
	}

	@Override
	protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
		if (newResource.isEmpty() || newAmount <= 0) {
			return accessResource.with(ModDataComponents.INJECTOR_CONTENTS.get(), InjectorContents.EMPTY);
		}
		return accessResource.with(ModDataComponents.INJECTOR_CONTENTS.get(), new InjectorContents(newResource.toStack(1), (short) newAmount));
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return itemAccess.getResource().is(validItem) && resource.getItem() instanceof SerumContainer;
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return maxSlotSize;
	}

}
