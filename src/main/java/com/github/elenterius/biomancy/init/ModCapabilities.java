package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.base.BlockEntityDelegator;
import com.github.elenterius.biomancy.block.bioforge.BioForgeBlockEntity;
import com.github.elenterius.biomancy.block.biolab.BioLabBlockEntity;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlockEntity;
import com.github.elenterius.biomancy.block.decomposer.DecomposerBlockEntity;
import com.github.elenterius.biomancy.block.digester.DigesterBlockEntity;
import com.github.elenterius.biomancy.block.mawhopper.MawHopperBlockEntity;
import com.github.elenterius.biomancy.block.modularlarynx.ModularLarynxBlockEntity;
import com.github.elenterius.biomancy.block.storagesac.StorageSacBlockEntity;
import com.github.elenterius.biomancy.inventory.InjectorItemAccessHandler;
import com.github.elenterius.biomancy.inventory.ItemHandlerResourceAdapter;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class ModCapabilities {

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BiomancyMod.MOD_ID);

	@Deprecated
	public static final Supplier<AttachmentType<FlagCapImpl>> NO_KNOCKBACK_FLAG_CAP = ATTACHMENT_TYPES.register("no_knockback", () -> AttachmentType.builder(FlagCapImpl::new).build());

	private ModCapabilities() {}

	private static @Nullable ResourceHandler<ItemResource> wrap(@Nullable IItemHandlerModifiable itemHandler) {
		return itemHandler != null ? new ItemHandlerResourceAdapter(itemHandler) : null;
	}

	@SubscribeEvent
	public static void onRegisterCapabilities(final RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.DIGESTER.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.DIGESTER.get(), DigesterBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.DECOMPOSER.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.DECOMPOSER.get(), DecomposerBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.BIO_LAB.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.BIO_LAB.get(), BioLabBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.BIO_FORGE.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.BIO_FORGE.get(), BioForgeBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.PRIMORDIAL_CRADLE.get(), PrimordialCradleBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.MAW_HOPPER.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.MODULAR_LARYNX.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.STORAGE_SAC.get(), (be, side) -> wrap(be.getItemHandler(side)));
		event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.BE_DELEGATOR.get(), (delegator, side) -> delegator.getDelegateCapability(Capabilities.Item.BLOCK, side));
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.BE_DELEGATOR.get(), (delegator, side) -> delegator.getDelegateCapability(Capabilities.Fluid.BLOCK, side));

		event.registerItem(Capabilities.Item.ITEM, (stack, access) -> new InjectorItemAccessHandler(access, InjectorItem.MAX_SLOT_SIZE), ModItems.INJECTOR.get());
		event.registerItem(Capabilities.Item.ITEM, (stack, access) -> new ItemAccessItemHandler(access, DataComponents.CONTAINER, StorageSacBlockEntity.SLOTS) {
			@Override
			protected int getCapacity(int index, ItemResource resource) {
				return Math.min(64, super.getCapacity(index, resource));
			}
		}, ModItems.STORAGE_SAC.get());
	}

	public interface IFlagCap {
		boolean isEnabled();

		void set(boolean enabled);

		default void enable() {
			set(true);
		}

		default void disable() {
			set(false);
		}

		default void toggle() {
			set(!isEnabled());
		}
	}

	public static class FlagCapImpl implements IFlagCap {
		private boolean isEnabled = false;

		@Override
		public boolean isEnabled() {
			return isEnabled;
		}

		@Override
		public void set(boolean enabled) {
			isEnabled = enabled;
		}

	}

}
