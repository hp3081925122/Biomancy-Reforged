package com.github.elenterius.biomancy.block.decomposer;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.HolderLookup;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.api.nutrients.FuelHandler;
import com.github.elenterius.biomancy.api.nutrients.FuelHandlerImpl;
import com.github.elenterius.biomancy.block.base.MachineBlock;
import com.github.elenterius.biomancy.block.base.MachineBlockEntity;
import com.github.elenterius.biomancy.crafting.VariableOutput;
import com.github.elenterius.biomancy.crafting.recipe.DecomposingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.SimpleRecipeType;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.inventory.InventoryHandler;
import com.github.elenterius.biomancy.inventory.InventoryHandlers;
import com.github.elenterius.biomancy.inventory.ItemHandlerUtil;
import com.github.elenterius.biomancy.menu.DecomposerMenu;
import com.github.elenterius.biomancy.sounds.LoopingSoundHelper;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DecomposerBlockEntity extends MachineBlockEntity<DecomposingRecipe, DecomposerStateData> implements MenuProvider, GeoBlockEntity {

	public static final int FUEL_SLOTS = 1;
	public static final int INPUT_SLOTS = DecomposingRecipe.MAX_INGREDIENTS;
	public static final int OUTPUT_SLOTS = DecomposingRecipe.MAX_OUTPUTS;

	public static final int MAX_FUEL = 1_000;

	public static final DeferredHolder<RecipeType<?>, SimpleRecipeType.AdvancedRecipeType<DecomposingRecipe>> RECIPE_TYPE = ModRecipes.DECOMPOSING_RECIPE_TYPE;

	protected static final RawAnimation WORKING_ANIM = RawAnimation.begin().thenLoop("decomposer.working");
	protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("decomposer.idle");

	private final DecomposerStateData stateData;
	private final FuelHandlerImpl fuelHandler;
	private final InventoryHandler<?> fuelInventory;
	private final InventoryHandler<?> inputInventory;
	private final InventoryHandler<?> outputInventory;

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private LoopingSoundHelper loopingSoundHelper = LoopingSoundHelper.NULL;

	private @Nullable DecomposerRecipeResult computedRecipeResult;

	public DecomposerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DECOMPOSER.get(), pos, state);

		inputInventory = InventoryHandlers.standard(INPUT_SLOTS, this::onInventoryChanged);
		outputInventory = InventoryHandlers.denyInput(OUTPUT_SLOTS, this::onInventoryChanged);

		fuelInventory = InventoryHandlers.filterFuel(FUEL_SLOTS, this::onInventoryChanged);
		fuelHandler = FuelHandlerImpl.createNutrientFuelHandler(MAX_FUEL, this::onInventoryChanged);

		stateData = new DecomposerStateData(fuelHandler);
	}

	@Override
	public void onLoad() {
		if (level != null && level.isClientSide()) {
			loopingSoundHelper = SoundUtil.Client.createLoopingSoundHandler();
		}
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Override
	public Component getName() {
		return BiomancyMod.translatableFrom("container", "decomposer");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return DecomposerMenu.createServerMenu(containerId, playerInventory, this);
	}

	@Override
	public DecomposerStateData getStateData() {
		return stateData;
	}

	@Override
	public InventoryHandler<?> getInputInventory() {
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
	protected boolean doesRecipeResultFitIntoOutputInv(DecomposingRecipe craftingGoal, ItemStack ignored) {
		DecomposerRecipeResult precomputedResult = getComputedRecipeResult(craftingGoal);
		return ItemHandlerUtil.doAllItemsFit(outputInventory.getRaw(), precomputedResult.items);
	}

	DecomposerRecipeResult getComputedRecipeResult(DecomposingRecipe craftingGoal) {
		if (computedRecipeResult == null || computedRecipeResult.recipe != craftingGoal) {
			Identifier recipeId = stateData.getCraftingGoalRecipe(level)
					.filter(holder -> holder.value() == craftingGoal)
					.map(holder -> holder.id().identifier())
					.orElse(null);
			return DecomposerRecipeResult.computeRecipeResult(recipeId, craftingGoal, level.getRandom().nextInt());
		}

		return computedRecipeResult;
	}

	@Override
	protected @Nullable RecipeHolder<DecomposingRecipe> resolveRecipeFromInput(Level level) {
		return RECIPE_TYPE.get().getBestRecipeFor(level, inputInventory.getRecipeWrapper()).orElse(null);
	}

	@Override
	protected boolean doesRecipeMatchInput(DecomposingRecipe recipeToTest, Level level) {
		return recipeToTest.matches(inputInventory.getRecipeWrapper(), level);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		stateData.serialize(output);
		if (computedRecipeResult != null) {
			computedRecipeResult.serialize(output.child("ComputedRecipeResult"));
		}
		fuelHandler.serialize(output.child("Fuel"));
		fuelInventory.serialize(output.child("FuelSlots"));
		inputInventory.serialize(output.child("InputSlots"));
		outputInventory.serialize(output.child("OutputSlots"));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		stateData.deserialize(input);
		if (level instanceof ServerLevel serverLevel) {
			input.child("ComputedRecipeResult").ifPresent(resultInput -> computedRecipeResult = DecomposerRecipeResult.deserialize(resultInput, serverLevel.recipeAccess()));
		}
		fuelHandler.deserialize(input.childOrEmpty("Fuel"));
		fuelInventory.deserialize(input.childOrEmpty("FuelSlots"));
		inputInventory.deserialize(input.childOrEmpty("InputSlots"));
		outputInventory.deserialize(input.childOrEmpty("OutputSlots"));
	}

	@Override
	public void dropAllInvContents(Level level, BlockPos pos) {
		ItemHandlerUtil.dropContents(level, pos, fuelInventory);
		ItemHandlerUtil.dropContents(level, pos, inputInventory);
		ItemHandlerUtil.dropContents(level, pos, outputInventory);
	}

	@Override
	protected boolean craftRecipe(DecomposingRecipe recipeToCraft, Level level) {
		DecomposerRecipeResult precomputedResult = getComputedRecipeResult(recipeToCraft);

		if (!ItemHandlerUtil.doAllItemsFit(outputInventory.getRaw(), precomputedResult.items)) return false;

		inputInventory.extractItem(0, recipeToCraft.getIngredientQuantity().count(), false);

		for (ItemStack stack : precomputedResult.items) {
			ItemHandlerUtil.insertItem(outputInventory.getRaw(), stack);
		}

		SoundUtil.Server.playBlockSound((ServerLevel) level, getBlockPos(), ModSoundEvents.DECOMPOSER_CRAFTING_COMPLETED);

		setChanged();
		return true;
	}

	private <T extends DecomposerBlockEntity> void onSoundKeyframe(final KeyFrameEvent<T, SoundKeyframeData> event) {
		if (event.keyframeData().getSound().equals("eat") && level != null && !isRemoved()) {
			SoundUtil.Client.playBlockSound(level, getBlockPos(), ModSoundEvents.DECOMPOSER_EAT);
		}
	}

	private <T extends DecomposerBlockEntity> PlayState handleAnimationState(final AnimationTest<T> event) {
		Boolean isCrafting = event.animatable().getBlockState().getValue(MachineBlock.CRAFTING);

		if (Boolean.TRUE.equals(isCrafting)) {
			event.controller().setAnimation(WORKING_ANIM);
			loopingSoundHelper.startLoop(this, ModSoundEvents.DECOMPOSER_CRAFTING.get(), 0.65f);
		}
		else {
			event.controller().setAnimation(IDLE_ANIM);
			loopingSoundHelper.stopLoop();
		}

		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		AnimationController<DecomposerBlockEntity> controller = new AnimationController<>("controller", 10, this::handleAnimationState);
		controller.setSoundKeyframeHandler(this::onSoundKeyframe);
		controllers.add(controller);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void setRemoved() {
		if (level != null && level.isClientSide()) {
			loopingSoundHelper.clear();
		}
		super.setRemoved();
	}

	record DecomposerRecipeResult(@Nullable Identifier recipeId, DecomposingRecipe recipe, int seed, List<ItemStack> items) {

		@Nullable
		public static DecomposerRecipeResult deserialize(ValueInput input, RecipeManager recipeManager) {
			Identifier recipeId = input.getString("recipeId").map(Identifier::tryParse).orElse(null);
			if (recipeId == null) return null;

			Optional<RecipeHolder<?>> holder = recipeManager.byKey(ResourceKey.create(Registries.RECIPE, recipeId));
			if (holder.isPresent() && holder.get().value() instanceof DecomposingRecipe decomposingRecipe) {
				return computeRecipeResult(recipeId, decomposingRecipe, input.getIntOr("seed", 0));
			}

			return null;
		}

		public static DecomposerRecipeResult computeRecipeResult(@Nullable Identifier recipeId, DecomposingRecipe recipe, int seed) {
			RandomSource random = RandomSource.create(seed);

			List<ItemStack> items = new ArrayList<>();
			for (VariableOutput output : recipe.getOutputs()) {
				ItemStack stack = output.getItemStack(random);
				if (!stack.isEmpty()) items.add(stack);
			}

			return new DecomposerRecipeResult(recipeId, recipe, seed, items);
		}

		public void serialize(ValueOutput output) {
			if (recipeId != null) output.putString("recipeId", recipeId.toString());
			output.putInt("seed", seed);
		}

	}

	public @Nullable IItemHandlerModifiable getItemHandler(@Nullable Direction side) {
		if (isRemoved()) return null;
		if (side == null || side == Direction.DOWN) return outputInventory;
		if (side == Direction.UP) return inputInventory;
		return fuelInventory;
	}

	public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
		if (isRemoved()) return null;
		return fuelHandler.getFluidConsumer();
	}

}
