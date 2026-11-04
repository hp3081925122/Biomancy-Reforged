package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.mojang.serialization.MapCodec;
import com.github.elenterius.biomancy.util.BlockItemUtil;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.component.DataComponents;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlockEntity;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CradleCleansingRecipe extends CustomRecipe {

	public static final CradleCleansingRecipe INSTANCE = new CradleCleansingRecipe();
	public static final MapCodec<CradleCleansingRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, CradleCleansingRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	public static final RecipeSerializer<CradleCleansingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public boolean matches(CraftingInput inventory, Level level) {
		ItemStack cradle = ItemStack.EMPTY;
		ItemStack cleansingSerum = ItemStack.EMPTY;

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (stack.isEmpty()) continue;
			Item item = stack.getItem();

			if (item == ModItems.PRIMORDIAL_CRADLE.get()) {
				if (!cradle.isEmpty()) {
					return false;
				}
				cradle = stack;
			}
			else if (item == ModItems.CLEANSING_SERUM.get()) {
				if (!cleansingSerum.isEmpty()) {
					return false;
				}
				cleansingSerum = stack;
			}
		}

		return !cradle.isEmpty() && !cleansingSerum.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput inventory) {
		ItemStack cradle = ItemStack.EMPTY;
		ItemStack cleansingSerum = ItemStack.EMPTY;

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (stack.isEmpty()) continue;
			Item item = stack.getItem();

			if (item == ModItems.PRIMORDIAL_CRADLE.get()) {
				if (!cradle.isEmpty()) {
					return ItemStack.EMPTY;
				}
				cradle = stack.copy();
			}
			else if (item == ModItems.CLEANSING_SERUM.get()) {
				if (!cleansingSerum.isEmpty()) {
					return ItemStack.EMPTY;
				}
				cleansingSerum = stack;
			}
		}

		return !cradle.isEmpty() && !cleansingSerum.isEmpty() ? createItem(cradle) : ItemStack.EMPTY;
	}

	private ItemStack createItem(ItemStack stack) {
		TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (data != null && data.contains(PrimordialCradleBlockEntity.PROC_GEN_VALUES_KEY)) {
			CompoundTag tag = data.copyTagWithoutId();
			tag.remove(PrimordialCradleBlockEntity.PROC_GEN_VALUES_KEY);
			BlockItemUtil.setBlockEntityData(stack, data.type(), tag);
		}
		return stack;
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<CradleCleansingRecipe> getSerializer() {
		return ModRecipes.CRADLE_CLEANSING_SERIALIZER.get();
	}

}
