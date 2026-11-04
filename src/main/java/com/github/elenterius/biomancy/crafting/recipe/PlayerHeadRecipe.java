package com.github.elenterius.biomancy.crafting.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.mojang.serialization.MapCodec;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.component.EssenceData;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.item.EssenceItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class PlayerHeadRecipe extends CustomRecipe {

	public static final PlayerHeadRecipe INSTANCE = new PlayerHeadRecipe();
	public static final MapCodec<PlayerHeadRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerHeadRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	public static final RecipeSerializer<PlayerHeadRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public boolean matches(CraftingInput inventory, Level level) {
		boolean hasPlayerHead = false;
		boolean hasPlayerUUID = false;
		boolean hasExoticDust = false;

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (stack.isEmpty()) continue;
			Item item = stack.getItem();

			if (item == Items.PLAYER_HEAD) {
				if (hasPlayerHead) return false;
				hasPlayerHead = true;
			}
			else if (item == ModItems.EXOTIC_DUST.get()) {
				if (hasExoticDust) return false;
				hasExoticDust = true;
			}
			else if (item instanceof EssenceItem essenceItem) {
				if (hasPlayerUUID) return false;

				if (essenceItem.getEntityType(stack).filter(entityType -> entityType == EntityType.PLAYER).isPresent()) {
					hasPlayerUUID = essenceItem.getEntityUUID(stack).isPresent();
				}
			}
			else return false;
		}

		return hasPlayerHead && hasPlayerUUID && hasExoticDust;
	}

	@Override
	public ItemStack assemble(CraftingInput inventory) {
		UUID uuid = null;
		String playerName = null;

		boolean hasPlayerHead = false;
		boolean hasExoticDust = false;

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (stack.isEmpty()) continue;
			Item item = stack.getItem();

			if (item == Items.PLAYER_HEAD) {
				if (hasPlayerHead) return ItemStack.EMPTY;
				hasPlayerHead = true;
			}
			else if (item == ModItems.EXOTIC_DUST.get()) {
				if (hasExoticDust) return ItemStack.EMPTY;
				hasExoticDust = true;
			}
			else if (item instanceof EssenceItem essenceItem) {
				if (uuid != null) {
					return ItemStack.EMPTY;
				}
				if (essenceItem.getEntityType(stack).filter(entityType -> entityType == EntityType.PLAYER).isPresent()) {
					uuid = essenceItem.getEntityUUID(stack).orElse(null);
					EssenceData essenceData = stack.get(ModDataComponents.ESSENCE);
					playerName = essenceData != null ? essenceData.playerName().orElse("") : "";
				}
			}
			else return ItemStack.EMPTY;
		}

		return hasPlayerHead && uuid != null && hasExoticDust ? createPlayerHeadFrom(uuid, playerName) : ItemStack.EMPTY;
	}

	private ItemStack createPlayerHeadFrom(UUID uuid, String playerName) {
		ItemStack stack = Items.PLAYER_HEAD.getDefaultInstance();

		stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(uuid));
		if (playerName != null && !playerName.isEmpty()) {
			stack.set(DataComponents.ITEM_NAME, Component.translatable(Items.PLAYER_HEAD.getDescriptionId() + ".named", playerName));
		}

		return stack;
	}

	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<PlayerHeadRecipe> getSerializer() {
		return ModRecipes.PLAYER_HEAD_SERIALIZER.get();
	}

}
