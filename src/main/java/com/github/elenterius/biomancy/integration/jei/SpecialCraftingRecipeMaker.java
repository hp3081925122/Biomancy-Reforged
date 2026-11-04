package com.github.elenterius.biomancy.integration.jei;

import com.github.elenterius.biomancy.util.BlockItemUtil;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.block.cradle.PrimordialCradleBlockEntity;
import com.github.elenterius.biomancy.block.membrane.BiometricMembraneBlock;
import com.github.elenterius.biomancy.component.EssenceData;
import com.github.elenterius.biomancy.init.ModBlockEntities;
import com.github.elenterius.biomancy.init.ModDataComponents;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.item.EssenceItem;
import com.github.elenterius.biomancy.item.armor.AcolyteArmorUpgrades;
import com.github.elenterius.biomancy.item.armor.LivingArmorItem;
import com.github.elenterius.biomancy.world.mound.MoundShape;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public final class SpecialCraftingRecipeMaker {

	private SpecialCraftingRecipeMaker() {}

	private static ShapelessRecipe shapeless(String group, ItemStack result, List<Ingredient> inputs) {
		return new ShapelessRecipe(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group), ItemStackTemplate.fromNonEmptyStack(result), inputs);
	}

	private static RecipeHolder<CraftingRecipe> holder(Identifier id, CraftingRecipe recipe) {
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe);
	}

	public static List<RecipeHolder<CraftingRecipe>> createCradleCleansingRecipes() {
		ItemStack cradle = ModItems.PRIMORDIAL_CRADLE.get().getDefaultInstance();
		CompoundTag tag = new CompoundTag();
		CompoundTag tagProcGen = new CompoundTag();
		MoundShape.ProcGenValues procGenValues = new MoundShape.ProcGenValues(1234L, (byte) 0, (byte) 0, (byte) 1, 250, 66, 0.7f, 0.5f);
		procGenValues.writeTo(tagProcGen);
		tag.put(PrimordialCradleBlockEntity.PROC_GEN_VALUES_KEY, tagProcGen);
		BlockItemUtil.setBlockEntityData(cradle, ModBlockEntities.PRIMORDIAL_CRADLE.get(), tag);

		List<Ingredient> ingredients = new ArrayList<>();
		ingredients.add(DataComponentIngredient.of(false, cradle));
		ingredients.add(Ingredient.of(ModItems.CLEANSING_SERUM.get()));
		List<Ingredient> inputs = List.copyOf(ingredients);

		Identifier recipeId = ModItems.PRIMORDIAL_CRADLE.getId().withSuffix("_cleansing" + ".cleansed");

		ItemStack result = ModItems.PRIMORDIAL_CRADLE.get().getDefaultInstance();

		return List.of(
				holder(recipeId, shapeless("", result, inputs))
		);
	}

	public static List<RecipeHolder<CraftingRecipe>> createHelmetUpgradeRecipes() {
		return ModItems.findEntries(LivingArmorItem.class).map(SpecialCraftingRecipeMaker::createHelmetUpgradeRecipe).toList();
	}

	private static <A extends Item> RecipeHolder<CraftingRecipe> createHelmetUpgradeRecipe(DeferredHolder<Item, A> armorItem) {
		List<Ingredient> inputs = List.of(Ingredient.of(ModItems.PRIMORDIAL_CORE.get()), Ingredient.of(armorItem.get()));
		ItemStack result = AcolyteArmorUpgrades.addUpgrade(armorItem.get().getDefaultInstance(), AcolyteArmorUpgrades.PRIMORDIAL_SIGHT);
		return holder(BiomancyMod.rl("special_crafting/" + armorItem.getId().getPath() + "_upgrade"), shapeless("", result, inputs));
	}

	public static List<RecipeHolder<CraftingRecipe>> createPlayerHeadRecipes() {
		List<Ingredient> inputs = List.of(
				Ingredient.of(Items.PLAYER_HEAD),
				Ingredient.of(ModItems.EXOTIC_DUST.get()),
				DataComponentIngredient.of(false, createUniquePlayerEssence(UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"), "jeb_"))
		);
		ItemStack result = createPlayerHead("jeb_");

		return List.of(
				holder(BiomancyMod.rl("special_crafting/player_head"), shapeless("", result, inputs))
		);
	}

	private static ItemStack createPlayerHead(String name) {
		ItemStack stack = Items.PLAYER_HEAD.getDefaultInstance();
		stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(name));

		return stack;
	}

	private static ItemStack createUniquePlayerEssence(UUID entityUUID, String playerName) {
		ItemStack stack = ModItems.ESSENCE.get().getDefaultInstance();

		EntityType<?> entityType = EntityType.PLAYER;
		int[] colors = EssenceItem.getEssenceColors(entityUUID);

		List<Integer> colorList = Arrays.stream(colors).boxed().toList();
		stack.set(ModDataComponents.ESSENCE, new EssenceData(EntityType.getKey(entityType), entityType.getDescriptionId(), Optional.of(entityUUID), colorList, 3, Optional.empty(), Optional.of(playerName)));

		return stack;
	}

	public static List<RecipeHolder<CraftingRecipe>> createBiometricMembraneRecipes() {
		UUID pigUUID = UUID.fromString("420faf42-bf42-4b20-af42-c42420e42d42");

		UUID playerUUID = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");
		String playerName = "jeb_";

		Stream<RecipeHolder<CraftingRecipe>[]> recipePairs = Stream.of(
				createRecipePair(EntityType.PIG, false, null),
				createRecipePair(EntityType.PIG, true, null),
				createRecipePair(EntityType.PIG, false, pigUUID),
				createRecipePair(EntityType.PIG, true, pigUUID),
				createRecipePair(EntityType.PLAYER, false, null),
				createRecipePair(EntityType.PLAYER, true, null),
				createUniquePlayerRecipePair(false, playerUUID, playerName),
				createUniquePlayerRecipePair(true, playerUUID, playerName)
		);

		return recipePairs.flatMap(Stream::of).toList();
	}

	private static RecipeHolder<CraftingRecipe>[] createUniquePlayerRecipePair(boolean isInverted, UUID playerUUID, String playerName) {
		Ingredient ingredient = DataComponentIngredient.of(false, createUniquePlayerEssence(playerUUID, playerName));
		return createRecipePair(EntityType.PLAYER, isInverted, playerUUID, 3, ingredient);
	}

	private static RecipeHolder<CraftingRecipe>[] createRecipePair(EntityType<?> entityType, boolean isInverted, @Nullable UUID entityUUID) {
		int tier = entityUUID != null ? 3 : 0;
		Ingredient ingredient = DataComponentIngredient.of(false, EssenceItem.fromEntityType(entityType, tier));
		return createRecipePair(entityType, isInverted, entityUUID, tier, ingredient);
	}

	@SuppressWarnings("unchecked")
	private static RecipeHolder<CraftingRecipe>[] createRecipePair(EntityType<?> entityType, boolean isInverted, @Nullable UUID entityUUID, int tier, Ingredient essence) {
		List<Ingredient> ingredients = new ArrayList<>();
		ingredients.add(essence);
		ingredients.add(Ingredient.of(ModItems.BIOMETRIC_MEMBRANE.get()));

		if (isInverted) {
			ingredients.add(Ingredient.of(Items.REDSTONE_TORCH));
		}

		List<Ingredient> inputs = List.copyOf(ingredients);

		ItemStack membraneStack = BiometricMembraneBlock.createItem(entityType, entityUUID, EssenceItem.getEssenceColors(entityType, entityUUID, tier), isInverted);

		String name = ModItems.BIOMETRIC_MEMBRANE.getId().toLanguageKey();
		String inverted = isInverted ? "inverted." : "";
		String unique = entityUUID != null ? "unique." : "";

		Identifier createRecipeId = BiomancyMod.rl(name + "." + inverted + unique + entityType.getDescriptionId());
		Identifier resetRecipeId = BiomancyMod.rl(name + ".reset." + inverted + unique + entityType.getDescriptionId());

		return new RecipeHolder[]{
				holder(createRecipeId, shapeless(name, membraneStack, inputs)),
				holder(resetRecipeId, shapeless(name, new ItemStack(ModItems.BIOMETRIC_MEMBRANE.get()), List.of(DataComponentIngredient.of(false, membraneStack)))),
		};
	}
}
