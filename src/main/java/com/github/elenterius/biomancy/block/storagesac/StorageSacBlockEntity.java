package com.github.elenterius.biomancy.block.storagesac;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.HolderLookup;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.base.SimpleContainerBlockEntity;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.inventory.InventoryHandler;
import com.github.elenterius.biomancy.inventory.InventoryHandlers;
import com.github.elenterius.biomancy.inventory.ItemHandlerUtil;
import com.github.elenterius.biomancy.menu.StorageSacMenu;
import com.github.elenterius.biomancy.util.ItemStackCounter;
import com.github.elenterius.biomancy.util.PlayerInteractionPredicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageSacBlockEntity extends SimpleContainerBlockEntity implements PlayerInteractionPredicate {

	public static final int SLOTS = 3 * 5;
	public static final String TOP5_BY_COUNT_KEY = "Top5ByCount";
	public static final String INVENTORY_KEY = "Inventory";
	private final InventoryHandler<?> inventory;
	protected final ItemStackCounter itemCounter = new ItemStackCounter();

	private List<ItemStackCounter.CountedItem> top5ItemsByCount = List.of();

	public static final String LOOT_TABLE_KEY = RandomizableContainer.LOOT_TABLE_TAG;
	public static final String LOOT_TABLE_SEED_TAG = RandomizableContainer.LOOT_TABLE_SEED_TAG;

	protected @Nullable ResourceKey<LootTable> lootTableId;
	protected long lootTableSeed;

	public StorageSacBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.STORAGE_SAC.get(), pos, state);
		inventory = InventoryHandlers.denyItemWithFilledInventory(SLOTS, this::onInventoryChanged);
	}

	protected boolean tryLoadLootTable(ValueInput input) {
		ResourceKey<LootTable> lootTable = input.read(LOOT_TABLE_KEY, LootTable.KEY_CODEC).orElse(null);
		if (lootTable == null) return false;

		lootTableId = lootTable;
		lootTableSeed = input.getLongOr(LOOT_TABLE_SEED_TAG, 0L);
		return true;
	}

	protected boolean trySaveLootTable(ValueOutput output) {
		if (lootTableId == null) return false;

		output.store(LOOT_TABLE_KEY, LootTable.KEY_CODEC, lootTableId);
		if (lootTableSeed != 0L) {
			output.putLong(LOOT_TABLE_SEED_TAG, lootTableSeed);
		}
		return true;
	}

	public void unpackLootTable(@Nullable Player player) {
		if (lootTableId == null) return;
		if (!(level instanceof ServerLevel serverLevel)) return;

		LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableId);

		if (player instanceof ServerPlayer) {
			CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer) player, lootTableId);
		}

		lootTableId = null;
		LootParams.Builder builder = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition));
		if (player != null) {
			builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
		}

		SimpleContainer container = new SimpleContainer(inventory.getSlots());
		for (int i = 0; i < inventory.getSlots(); i++) {
			container.setItem(i, inventory.getStackInSlot(i));
		}
		lootTable.fill(container, builder.create(LootContextParamSets.CHEST), lootTableSeed);
		for (int i = 0; i < inventory.getSlots(); i++) {
			inventory.setStackInSlot(i, container.getItem(i));
		}
	}

	public void setLootTable(ResourceKey<LootTable> lootTableId, long lootTableSeed) {
		this.lootTableId = lootTableId;
		this.lootTableSeed = lootTableSeed;
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		if (lootTableId != null && player.isSpectator()) return null;
		return StorageSacMenu.createServerMenu(containerId, playerInventory, this);
	}

	public InventoryHandler<?> getInventory() {
		unpackLootTable(null);
		return inventory;
	}

	@Override
	public Component getDefaultName() {
		return BiomancyMod.translatableFrom("container", "sac");
	}

	public List<ItemStackCounter.CountedItem> getItemsForRendering() {
		return top5ItemsByCount;
	}

	protected void countAllItems() {
		itemCounter.clear();
		itemCounter.accountStacks(inventory);
		top5ItemsByCount = itemCounter.getItemCountSorted(5, false);
	}

	public boolean isEmpty() {
		unpackLootTable(null);
		return inventory.isEmpty();
	}

	protected void onInventoryChanged() {
		if (level == null || level.isClientSide()) return;

		countAllItems();
		setChanged();
		syncToClient();
	}

	protected void syncToClient() {
		if (level == null || level.isClientSide()) return;

		BlockState state = getBlockState();
		level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		serializeTop5(output.child(TOP5_BY_COUNT_KEY));
		return output.buildResult();
	}

	@Override
	public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		if (trySaveLootTable(output)) return;

		inventory.serialize(output.child(INVENTORY_KEY));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		if (tryLoadLootTable(input)) return;

		input.child(INVENTORY_KEY).ifPresent(inventoryInput -> {
			inventory.deserialize(inventoryInput);
			countAllItems();
		});

		input.child(TOP5_BY_COUNT_KEY).ifPresent(top5Input -> top5ItemsByCount = deserializeTop5(top5Input));
	}

	public List<ItemStackCounter.CountedItem> deserializeTop5(ValueInput input) {
		List<ItemStackCounter.CountedItem> items = new ArrayList<>();
		input.childrenListOrEmpty("Items").stream().forEach(itemInput -> {
			ItemStack stack = itemInput.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY);
			if (!stack.isEmpty()) {
				int amount = itemInput.getIntOr("Amount", 0);
				stack.setCount(Mth.clamp(amount, 1, 3));
				items.add(new ItemStackCounter.CountedItem(stack, amount));
			}
		});
		return items;
	}

	public void serializeTop5(ValueOutput output) {
		ValueOutput.ValueOutputList list = output.childrenList("Items");
		for (ItemStackCounter.CountedItem countedItem : top5ItemsByCount) {
			if (countedItem.stack().isEmpty()) continue;
			ValueOutput itemOutput = list.addChild();
			itemOutput.store(ItemStack.MAP_CODEC, countedItem.stack().copyWithCount(1));
			itemOutput.putInt("Amount", countedItem.amount());
		}
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter componentInput) {
		super.applyImplicitComponents(componentInput);
		ItemContainerContents contents = componentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
		contents.copyInto(stacks);
		for (int i = 0; i < stacks.size(); i++) {
			inventory.setStackInSlot(i, stacks.get(i));
		}
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < inventory.getSlots(); i++) {
			stacks.add(inventory.getStackInSlot(i));
		}
		components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		super.removeComponentsFromTag(output);
		output.discard(INVENTORY_KEY);
	}

	public @Nullable IItemHandlerModifiable getItemHandler(@Nullable Direction side) {
		if (isRemoved()) return null;
		unpackLootTable(null);
		return inventory;
	}

	@Override
	public void dropContainerContents(Level level, BlockPos pos) {
		unpackLootTable(null);
		ItemHandlerUtil.dropContents(level, pos, inventory);
	}

}
