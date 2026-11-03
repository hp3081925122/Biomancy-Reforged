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
import com.github.elenterius.biomancy.inventory.InjectorItemInventory;
import com.github.elenterius.biomancy.inventory.ItemStackInventory;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BiomancyMod.MOD_ID);

	@Deprecated
	public static final Supplier<AttachmentType<FlagCapImpl>> NO_KNOCKBACK_FLAG_CAP = ATTACHMENT_TYPES.register("no_knockback", () -> AttachmentType.builder(FlagCapImpl::new).build());

	private ModCapabilities() {}

	@SubscribeEvent
	public static void onRegisterCapabilities(final RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DIGESTER.get(), DigesterBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.DIGESTER.get(), DigesterBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DECOMPOSER.get(), DecomposerBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.DECOMPOSER.get(), DecomposerBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.BIO_LAB.get(), BioLabBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.BIO_LAB.get(), BioLabBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.BIO_FORGE.get(), BioForgeBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.BIO_FORGE.get(), BioForgeBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.PRIMORDIAL_CRADLE.get(), PrimordialCradleBlockEntity::getFluidHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MAW_HOPPER.get(), MawHopperBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MODULAR_LARYNX.get(), ModularLarynxBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.STORAGE_SAC.get(), StorageSacBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.BE_DELEGATOR.get(), (delegator, side) -> delegator.getDelegateCapability(Capabilities.ItemHandler.BLOCK, side));
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.BE_DELEGATOR.get(), (delegator, side) -> delegator.getDelegateCapability(Capabilities.FluidHandler.BLOCK, side));

		event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> InjectorItemInventory.create(InjectorItem.MAX_SLOT_SIZE, stack).getItemHandler(), ModItems.INJECTOR.get());
		event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> ItemStackInventory.create(StorageSacBlockEntity.SLOTS, 64, stack).getItemHandler(), ModItems.STORAGE_SAC.get());
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
