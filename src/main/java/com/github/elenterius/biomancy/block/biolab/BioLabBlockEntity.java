package com.github.elenterius.biomancy.block.biolab;

import net.minecraft.core.HolderLookup;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.nutrients.FuelHandler;
import com.github.elenterius.biomancy.api.nutrients.FuelHandlerImpl;
import com.github.elenterius.biomancy.api.nutrients.Nutrients;
import com.github.elenterius.biomancy.block.base.MachineBlock;
import com.github.elenterius.biomancy.block.base.MachineBlockEntity;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.recipe.BioBrewingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.PotionSerumRecipes;
import com.github.elenterius.biomancy.crafting.recipe.SimpleRecipeType;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.inventory.BehavioralItemHandler;
import com.github.elenterius.biomancy.inventory.InventoryHandler;
import com.github.elenterius.biomancy.inventory.InventoryHandlers;
import com.github.elenterius.biomancy.inventory.ItemHandlerUtil;
import com.github.elenterius.biomancy.menu.BioLabMenu;
import com.github.elenterius.biomancy.sounds.LoopingSoundHelper;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BioLabBlockEntity extends MachineBlockEntity<BioBrewingRecipe, BioLabStateData> implements MenuProvider, GeoBlockEntity {

	public static final int FUEL_SLOTS = 1;
	public static final int INPUT_SLOTS = BioBrewingRecipe.MAX_INGREDIENTS + BioBrewingRecipe.MAX_REACTANT;
	public static final int OUTPUT_SLOTS = 1;

	public static final int MAX_FUEL = 1_000;

	public static final DeferredHolder<RecipeType<?>, SimpleRecipeType.AdvancedRecipeType<BioBrewingRecipe>> RECIPE_TYPE = ModRecipes.BIO_BREWING_RECIPE_TYPE;

	protected static final RawAnimation WORKING_ANIM = RawAnimation.begin().thenLoop("bio_lab.working");
	protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("bio_lab.idle");

	private final BioLabStateData stateData;
	private final FuelHandlerImpl fuelHandler;
	private final InventoryHandler<?> fuelInventory;

	private final InventoryHandler<BehavioralItemHandler.LockableItemStackFilterInput> inputInventory;

	private final InventoryHandler<?> outputInventory;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private LoopingSoundHelper loopingSoundHelper = LoopingSoundHelper.NULL;

	private final IItemHandler combinedInventory;

	public BioLabBlockEntity(BlockPos worldPosition, BlockState blockState) {
		super(ModBlockEntities.BIO_LAB.get(), worldPosition, blockState);

		inputInventory = InventoryHandlers.lockableFilterInput(INPUT_SLOTS, this::onInventoryChanged);

		outputInventory = InventoryHandlers.denyInput(OUTPUT_SLOTS, this::onInventoryChanged);

		fuelInventory = InventoryHandlers.filterFuel(FUEL_SLOTS, this::onInventoryChanged);

		fuelHandler = FuelHandlerImpl.createNutrientFuelHandler(MAX_FUEL, this::setChanged);

		stateData = new BioLabStateData(fuelHandler, inputInventory.get());
		combinedInventory = createCombinedInventory();
	}

	private IItemHandler createCombinedInventory() {
		return new CombinedInvWrapper(
				fuelInventory,
				new RangedWrapper(inputInventory, inputInventory.getSlots() - 1, inputInventory.getSlots())) {
					@Override
					public boolean isItemValid(int slot, ItemStack stack) {
						return !Nutrients.FUEL_PREDICATE.test(stack) && super.isItemValid(slot, stack);
					}
				};
	}

	@Override
	public void onLoad() {
		if (level != null && level.isClientSide) {
			loopingSoundHelper = SoundUtil.Client.createLoopingSoundHandler();
		}
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Override
	public Component getName() {
		return BiomancyMod.translatableFrom("container", "bio_lab");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return BioLabMenu.createServerMenu(containerId, playerInventory, this);
	}

	@Override
	public BioLabStateData getStateData() {
		return stateData;
	}

	@Override
	public InventoryHandler<BehavioralItemHandler.LockableItemStackFilterInput> getInputInventory() {
		return inputInventory;
	}

	public InventoryHandler<?> getFuelInventory() {
		return fuelInventory;
	}

	public InventoryHandler<?> getOutputInventory() {
		return outputInventory;
	}

	@Override
	protected FuelHandler getFuelHandler() {
		return fuelHandler;
	}

	@Override
	public ItemStack getStackInFuelSlot() {
		return fuelInventory.getStackInSlot(0);
	}

	@Override
	public void setStackInFuelSlot(ItemStack stack) {
		fuelInventory.setStackInSlot(0, stack);
	}

	@Override
	protected boolean doesRecipeResultFitIntoOutputInv(BioBrewingRecipe craftingGoal, ItemStack stackToCraft) {
		return ItemHandlerUtil.doesItemFit(outputInventory.getRaw(), 0, stackToCraft);
	}

	@Override
	protected @Nullable BioBrewingRecipe resolveRecipeFromInput(Level level) {
		return RECIPE_TYPE.get()
				.getBestRecipeFor(level, inputInventory.getRecipeWrapper())
				.orElse(PotionSerumRecipes.getRecipeFor(level, inputInventory.getRecipeWrapper()));
	}

	@Override
	protected boolean doesRecipeMatchInput(BioBrewingRecipe recipeToTest, Level level) {
		return recipeToTest.matches(inputInventory.getRecipeWrapper(), level);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		stateData.serialize(tag);
		tag.put("Fuel", fuelHandler.serializeNBT(registries));
		tag.put("FuelSlots", fuelInventory.serializeNBT(registries));
		tag.put("InputSlots", inputInventory.serializeNBT(registries));
		tag.put("OutputSlots", outputInventory.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		stateData.deserialize(tag);
		fuelHandler.deserializeNBT(registries, tag.getCompound("Fuel"));
		fuelInventory.deserializeNBT(registries, tag.getCompound("FuelSlots"));
		inputInventory.deserializeNBT(registries, tag.getCompound("InputSlots"));
		outputInventory.deserializeNBT(registries, tag.getCompound("OutputSlots"));
	}

	@Override
	public void dropAllInvContents(Level level, BlockPos pos) {
		ItemHandlerUtil.dropContents(level, pos, fuelInventory);
		ItemHandlerUtil.dropContents(level, pos, inputInventory);
		ItemHandlerUtil.dropContents(level, pos, outputInventory);
	}

	@Override
	protected boolean craftRecipe(BioBrewingRecipe recipeToCraft, Level level) {
		ItemStack result = recipeToCraft.getResultItem(level.registryAccess()).copy();
		if (result.isEmpty() || !doesRecipeResultFitIntoOutputInv(recipeToCraft, result)) {
			return false;
		}

		List<IngredientStack> ingredients = recipeToCraft.getIngredientQuantities();
		int[] ingredientCost = new int[ingredients.size()];
		for (int i = 0; i < ingredients.size(); i++) {
			ingredientCost[i] = ingredients.get(i).count();
		}

		final int lastIndex = inputInventory.getSlots() - 1;
		inputInventory.extractItem(lastIndex, 1, false);

		for (int idx = 0; idx < lastIndex; idx++) {
			final ItemStack foundStack = inputInventory.getStackInSlot(idx);
			if (!foundStack.isEmpty()) {
				for (int i = 0; i < ingredients.size(); i++) {
					int remainingCost = ingredientCost[i];
					if (remainingCost > 0 && ingredients.get(i).testItem(foundStack)) {
						int amount = Math.min(remainingCost, foundStack.getCount());
						inputInventory.extractItem(idx, amount, false);
						ingredientCost[i] -= amount;
						break;
					}
				}
			}
		}

		outputInventory.getRaw().insertItem(0, result, false);

		SoundUtil.Server.playBlockSound((ServerLevel) level, getBlockPos(), ModSoundEvents.BIO_LAB_CRAFTING_COMPLETED);

		setChanged();
		return true;
	}

	private <T extends BioLabBlockEntity> PlayState handleAnimationState(AnimationState<T> event) {
		boolean isCrafting = getBlockState().getValue(MachineBlock.CRAFTING);

		if (isCrafting) {
			event.getController().setAnimation(WORKING_ANIM);
			loopingSoundHelper.startLoop(this, ModSoundEvents.BIO_LAB_CRAFTING.get(), 0.65f);
		}
		else {
			event.getController().setAnimation(IDLE_ANIM);
			loopingSoundHelper.stopLoop();
		}

		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "main", 0, this::handleAnimationState));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void setRemoved() {
		if (level != null && level.isClientSide) {
			loopingSoundHelper.clear();
		}
		super.setRemoved();
	}

	public @Nullable IItemHandler getItemHandler(@Nullable Direction side) {
		if (isRemoved()) return null;
		if (side == null || side == Direction.DOWN) return outputInventory;
		if (side == Direction.UP) return inputInventory;
		return combinedInventory;
	}

	public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
		if (isRemoved()) return null;
		return fuelHandler.getFluidConsumer();
	}

}
