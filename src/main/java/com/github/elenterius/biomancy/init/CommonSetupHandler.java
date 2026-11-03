package com.github.elenterius.biomancy.init;

import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.entity.projectile.GrenadeProjectile;
import com.github.elenterius.biomancy.integration.ModsCompatHandler;
import com.github.elenterius.biomancy.item.extractor.ExtractorItem;
import com.github.elenterius.biomancy.item.injector.InjectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = BiomancyMod.MOD_ID)
public final class CommonSetupHandler {

	private CommonSetupHandler() {}

	@SubscribeEvent
	public static void onSetup(final FMLCommonSetupEvent event) {
		ModRecipeBookTypes.init();

		event.enqueueWork(() -> {
			registerDispenserBehaviors();
			AcidInteractions.register();
		});

		ModFluids.registerInteractions();
		ModsCompatHandler.onBiomancyCommonSetup(event);
	}

	@SubscribeEvent
	public static void onRegisterBrewingRecipes(final RegisterBrewingRecipesEvent event) {
		ModRecipes.registerBrewingRecipes(event.getBuilder());
	}

	private static void registerDispenserBehaviors() {
		DispenserBlock.registerBehavior(ModItems.ESSENCE_EXTRACTOR.get(), new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource source, ItemStack stack) {
				BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
				setSuccess(ExtractorItem.tryExtractEssence(source.level(), pos, stack));
				if (isSuccess()) {
					stack.hurtAndBreak(1, source.level(), (ServerPlayer) null, item -> {});
				}
				return stack;
			}
		});

		DispenserBlock.registerBehavior(ModItems.INJECTOR.get(), new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource source, ItemStack stack) {
				BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
				setSuccess(InjectorItem.tryInjectLivingEntity(source.level(), pos, stack));
				if (isSuccess()) {
					stack.hurtAndBreak(1, source.level(), (ServerPlayer) null, item -> {});
				}
				return stack;
			}
		});

		DispenserBlock.registerBehavior(ModItems.ACID_BUCKET.get(), new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
				ServerLevel level = source.level();

				DispensibleContainerItem containerItem = (DispensibleContainerItem) stack.getItem();
				if (containerItem.emptyContents(null, level, pos, null, stack)) {
					containerItem.checkExtraContent(null, level, stack, pos);
					return new ItemStack(Items.BUCKET);
				}
				else {
					return defaultDispenseItemBehavior.dispense(source, stack);
				}
			}
		});

		DispenseItemBehavior grenadeDispenseBehavior = new DefaultDispenseItemBehavior() {
			private static final float UNCERTAINTY = 6f * 0.5f;
			private static final float POWER = 1.1f * 1.25f;

			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				ServerLevel level = source.level();
				Position position = DispenserBlock.getDispensePosition(source);
				Direction direction = source.state().getValue(DispenserBlock.FACING);
				GrenadeProjectile grenade = new GrenadeProjectile(level, position.x(), position.y(), position.z());
				grenade.setItem(stack);
				grenade.shoot(direction.getStepX(), direction.getStepY() + 0.1f, direction.getStepZ(), POWER, UNCERTAINTY);
				level.addFreshEntity(grenade);
				stack.shrink(1);
				return stack;
			}

			@Override
			protected void playSound(BlockSource source) {
				source.level().levelEvent(1002, source.pos(), 0);
			}
		};

		DispenserBlock.registerBehavior(ModItems.TOXIN_GRENADE.get(), grenadeDispenseBehavior);
		DispenserBlock.registerBehavior(ModItems.ACID_GRENADE.get(), grenadeDispenseBehavior);
		DispenserBlock.registerBehavior(ModItems.DECAY_GRENADE.get(), grenadeDispenseBehavior);
		DispenserBlock.registerBehavior(ModItems.VOLATILE_GRENADE.get(), grenadeDispenseBehavior);
	}

}
