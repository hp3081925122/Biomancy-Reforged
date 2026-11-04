package com.github.elenterius.biomancy.crafting.recipe;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.datagen.recipes.builder.RecipeCostUtil;
import com.github.elenterius.biomancy.init.ModItems;
import com.github.elenterius.biomancy.serum.PotionSerum;
import com.github.elenterius.biomancy.util.ItemStackCounter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PotionSerumRecipes {

	public static final String NAME_PREFIX = "potion_serum_";

	private static final Map<PotionBrewing, Data> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

	private PotionSerumRecipes() {}

	public record Data(List<RecipeHolder<BioBrewingRecipe>> recipes, Map<Identifier, RecipeHolder<BioBrewingRecipe>> recipesById, Set<Holder<Potion>> potions) {}

	public static Data get(PotionBrewing potionBrewing) {
		return CACHE.computeIfAbsent(potionBrewing, PotionSerumRecipes::compute);
	}

	public static Data get(Level level) {
		return get(level.potionBrewing());
	}

	public static List<RecipeHolder<BioBrewingRecipe>> getRecipes(Level level) {
		return get(level).recipes();
	}

	public static Set<Holder<Potion>> getPotions(Level level) {
		return get(level).potions();
	}

	public static Optional<RecipeHolder<BioBrewingRecipe>> byId(Level level, @Nullable Identifier recipeId) {
		return recipeId == null ? Optional.empty() : Optional.ofNullable(get(level).recipesById().get(recipeId));
	}

	public static @Nullable RecipeHolder<BioBrewingRecipe> getRecipeFor(Level level, RecipeInput input) {
		RecipeHolder<BioBrewingRecipe> topRecipe = null;
		int topPriority = Integer.MIN_VALUE;

		for (RecipeHolder<BioBrewingRecipe> holder : getRecipes(level)) {
			if (!holder.value().matches(input, level)) continue;

			int currentPriority = holder.value().getMatchPriority();
			if (currentPriority > topPriority) {
				topRecipe = holder;
				topPriority = currentPriority;
			}
		}

		return topRecipe;
	}

	private static Data compute(PotionBrewing potionBrewing) {
		Set<Potion> bestPotions = getBestPotions();
		List<PotionRecipe> bestPotionRecipes = new ArrayList<>();
		Map<Potion, List<PotionRecipe>> recipeLookup = new HashMap<>();

		for (PotionBrewing.Mix<Potion> mix : potionBrewing.potionMixes) {
			PotionRecipe recipe = new PotionRecipe(mix.ingredient(), mix.from().value(), mix.to().value());
			recipeLookup.computeIfAbsent(recipe.result, potion -> new ArrayList<>()).add(recipe);
			if (bestPotions.contains(recipe.result)) {
				bestPotionRecipes.add(recipe);
			}
		}

		for (IBrewingRecipe iBrewingRecipe : potionBrewing.getRecipes()) {
			if (iBrewingRecipe instanceof BrewingRecipe brewingRecipe) {
				Potion result = getPotion(brewingRecipe.getOutput());
				Ingredient ingredient = brewingRecipe.getIngredient();
				Set<Potion> possibleReactants = getPossiblePotions(brewingRecipe.getInput());
				for (Potion reactant : possibleReactants) {
					PotionRecipe recipe = new PotionRecipe(ingredient, reactant, result);
					recipeLookup.computeIfAbsent(recipe.result, potion -> new ArrayList<>()).add(recipe);
					if (bestPotions.contains(result)) {
						bestPotionRecipes.add(recipe);
					}
				}
			}
		}

		List<RecipeHolder<BioBrewingRecipe>> recipes = new ArrayList<>();
		Set<Holder<Potion>> potions = new LinkedHashSet<>();
		int index = 0;

		for (PotionRecipe bestPotionRecipe : bestPotionRecipes) {
			List<ResolvedRecipe> resolvedRecipes = resolveRecipes(bestPotionRecipe, recipeLookup);
			Holder<Potion> resultHolder = BuiltInRegistries.POTION.wrapAsHolder(bestPotionRecipe.result);

			if (resolvedRecipes.size() <= 2) {
				for (ResolvedRecipe resolvedRecipe : resolvedRecipes) {
					recipes.add(createRecipe(NAME_PREFIX + index++, resolvedRecipe.ingredients, resolvedRecipe.reactant.getDefaultInstance(), resultHolder));
					potions.add(resultHolder);
				}
				continue;
			}

			ItemStackCounter itemCounter = new ItemStackCounter();
			for (ResolvedRecipe resolvedRecipe : resolvedRecipes) {
				for (Ingredient ingredient : resolvedRecipe.ingredients) {
					itemCounter.accountStacks(IngredientStack.getItems(ingredient));
				}
			}

			List<Ingredient> ingredients = itemCounter.getItemsSorted(4, false).stream().map(stack -> Ingredient.of(stack.getItem())).toList();

			recipes.add(createRecipe(NAME_PREFIX + index++, ingredients, ModItems.UNSTABLE_COMPOUND.get().getDefaultInstance(), resultHolder));
			potions.add(resultHolder);
		}

		Map<Identifier, RecipeHolder<BioBrewingRecipe>> recipesById = recipes.stream().collect(Collectors.toUnmodifiableMap(recipe -> recipe.id().identifier(), recipe -> recipe, (a, b) -> b));
		return new Data(Collections.unmodifiableList(recipes), recipesById, Collections.unmodifiableSet(potions));
	}

	private record PotionRecipe(Ingredient ingredient, @Nullable Potion potionIngredient, @Nullable Potion result) {}

	private record ResolvedRecipe(List<Ingredient> ingredients, Item reactant) {}

	private static List<ResolvedRecipe> resolveRecipes(PotionRecipe recipeRoot, Map<Potion, List<PotionRecipe>> recipeLookup) {
		List<ResolvedRecipe> accumulator = new ArrayList<>();
		resolveRecursive(recipeRoot, new ArrayList<>(), recipeLookup, accumulator::add);
		return accumulator;
	}

	private static void resolveRecursive(PotionRecipe current, List<Ingredient> currentIngredients, Map<Potion, List<PotionRecipe>> recipeLookup, Consumer<ResolvedRecipe> accumulator) {
		if (current.potionIngredient == Potions.WATER.value()) {
			List<Ingredient> ingredients = new ArrayList<>(currentIngredients);
			ingredients.add(current.ingredient);
			accumulator.accept(new ResolvedRecipe(ingredients, ModItems.ORGANIC_COMPOUND.get()));
			return;
		}

		if (current.potionIngredient == Potions.MUNDANE.value()) {
			List<Ingredient> ingredients = new ArrayList<>(currentIngredients);
			ingredients.add(current.ingredient);
			if (ingredients.size() < 4) {
				ingredients.add(Ingredient.of(ModItems.BILE.get()));
				accumulator.accept(new ResolvedRecipe(ingredients, ModItems.VIAL.get()));
			}
			else {
				accumulator.accept(new ResolvedRecipe(ingredients, ModItems.ORGANIC_COMPOUND.get()));
			}

			return;
		}

		if (current.potionIngredient == null || recipeLookup.getOrDefault(current.potionIngredient, List.of()).isEmpty()) {
			List<Ingredient> ingredients = new ArrayList<>(currentIngredients);
			ingredients.add(current.ingredient);
			accumulator.accept(new ResolvedRecipe(ingredients, ModItems.EXOTIC_COMPOUND.get()));
			return;
		}

		if (currentIngredients.size() >= 4) {
			return;
		}

		List<PotionRecipe> subRecipes = recipeLookup.get(current.potionIngredient);
		for (PotionRecipe subRecipe : subRecipes) {
			List<Ingredient> ingredients = new ArrayList<>(currentIngredients);
			ingredients.add(current.ingredient);
			resolveRecursive(subRecipe, ingredients, recipeLookup, accumulator);
		}
	}

	private static Set<Potion> getBestPotions() {
		Map<MobEffect, Map<Potion, MobEffectInstance>> primaryPotionEffects = new HashMap<>();
		for (Potion potion : BuiltInRegistries.POTION) {
			MobEffectInstance instance = PotionSerum.getPrimaryEffectInstance(potion.getEffects());
			if (instance == null) continue;
			primaryPotionEffects.computeIfAbsent(instance.getEffect().value(), k -> new HashMap<>()).put(potion, instance);
		}

		Set<Potion> bestPotions = new HashSet<>();

		for (Map<Potion, MobEffectInstance> candidates : primaryPotionEffects.values()) {
			MobEffectInstance instance = PotionSerum.getPrimaryEffectInstance(candidates.values());
			candidates.entrySet().stream()
					.filter(entry -> entry.getValue() == instance)
					.findAny()
					.map(Map.Entry::getKey)
					.ifPresent(bestPotions::add);
		}

		return bestPotions;
	}

	private static @Nullable Potion getPotion(ItemStack stack) {
		if (!stack.is(Items.POTION)) return null;
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(Holder::value).orElse(null);
	}

	private static Set<Potion> getPossiblePotions(Ingredient ingredient) {
		Set<Potion> possiblePotions = new HashSet<>();

		for (ItemStack stack : IngredientStack.getItems(ingredient)) {
			Potion potion = getPotion(stack);
			if (potion != null) possiblePotions.add(potion);
		}

		return possiblePotions;
	}

	private static RecipeHolder<BioBrewingRecipe> createRecipe(String name, List<Ingredient> ingredients, ItemStack reactant, Holder<Potion> result) {
		Identifier id = BiomancyMod.rl(name);

		List<IngredientStack> ingredientStacks = new ArrayList<>();
		for (Ingredient ingredient : ingredients) {
			ingredientStacks.add(new IngredientStack(ingredient, 1));
		}

		int craftingTimeTicks = BioBrewingRecipe.DEFAULT_CRAFTING_TIME_TICKS + ingredients.size() * 20;
		int craftingCostNutrients = RecipeCostUtil.getCost(BioBrewingRecipe.DEFAULT_CRAFTING_COST_NUTRIENTS, craftingTimeTicks);

		BioBrewingRecipe recipe = new BioBrewingRecipe(
				ModItems.POTION_SERUM.get().getInstanceFrom(result),
				craftingTimeTicks,
				craftingCostNutrients,
				ingredientStacks,
				Optional.of(Ingredient.of(reactant.getItem()))
		);
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe);
	}

}
