package com.github.elenterius.biomancy.client.gui;

import com.github.elenterius.biomancy.BiomancyConfig;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.recipe.BioForgingRecipe;
import com.github.elenterius.biomancy.crafting.recipe.RecipeMaps;
import com.github.elenterius.biomancy.init.ModBioForgeTabs;
import com.github.elenterius.biomancy.init.ModRecipeBookTypes;
import com.github.elenterius.biomancy.init.ModRecipes;
import com.github.elenterius.biomancy.menu.BioForgeMenu;
import com.github.elenterius.biomancy.menu.BioForgeTab;
import com.github.elenterius.biomancy.network.ModNetworkHandler;
import com.github.elenterius.biomancy.util.ItemStackCounter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

class BioForgeScreenController {

	public static final int ROWS = 6;
	public static final int COLS = 4;
	public static final int GRID_SIZE = COLS * ROWS;
	private static final Comparator<BioForgeTab> CATEGORY_COMPARATOR = (a, b) -> {
		if (a.sortPriority() == b.sortPriority()) return a.enumId().compareTo(b.enumId());
		return b.sortPriority() - a.sortPriority();
	};
	private static RecipeSelection recipeSelection = RecipeSelection.EMPTY;
	private final ItemStackCounter itemCounter;
	private final List<BioForgeTab> tabs;
	private final Minecraft minecraft;
	private final BioForgeMenu menu;
	private final Set<ResourceKey<Recipe<?>>> craftableRecipes = new HashSet<>();
	private List<RecipeHolder<BioForgingRecipe>> allRecipes = List.of();
	private SearchTree<RecipeHolder<BioForgingRecipe>> searchTree = SearchTree.empty();
	private int maxPages = 0;
	private int activeTab = 0;
	private int startIndex = 0;
	private int playerInvChanges;
	private String currentSearchString = "";
	private List<RecipeHolder<BioForgingRecipe>> shownRecipes = List.of();
	private int crossoverGridIndex = 0;

	public BioForgeScreenController(Minecraft minecraft, BioForgeMenu menu) {
		this.minecraft = minecraft;
		this.menu = menu;

		tabs = ModBioForgeTabs.REGISTRY.stream().sorted(CATEGORY_COMPARATOR).toList();

		playerInvChanges = getPlayer().getInventory().getTimesChanged();
		itemCounter = new ItemStackCounter();
		itemCounter.accountStacks(getPlayer().getInventory().getNonEquipmentItems());

		if (recipeSelection != RecipeSelection.EMPTY && menu.getSelectedRecipe() == null && recipeSelection.recipe != null) {
			ModNetworkHandler.sendBioForgeRecipeToServer(menu.containerId, recipeSelection.recipe);
		}

		reloadRecipes();
		updateAndSearchRecipes();
	}

	private ClientLevel getLevel() {
		return Objects.requireNonNull(minecraft.level);
	}

	private LocalPlayer getPlayer() {
		return Objects.requireNonNull(minecraft.player);
	}

	public void resetPagination() {
		startIndex = 0;
	}

	public int getCurrentPage() {
		return startIndex / GRID_SIZE + 1;
	}

	public int getMaxPages() {
		return maxPages;
	}

	public void goToNextPage() {
		if (hasNextPage()) {
			startIndex += GRID_SIZE;
		}
	}

	public boolean hasNextPage() {
		return getCurrentPage() < maxPages;
	}

	public boolean hasPrevPage() {
		return getCurrentPage() > 1;
	}

	public void goToPrevPage() {
		if (hasPrevPage()) {
			startIndex -= GRID_SIZE;
		}
	}

	public boolean hasRecipesOnPage() {
		return !shownRecipes.isEmpty();
	}

	private RecipeHolder<BioForgingRecipe> getRecipe(int index) {
		if (index >= shownRecipes.size()) throw new IndexOutOfBoundsException(index);
		return shownRecipes.get(index);
	}

	public RecipeHolder<BioForgingRecipe> getRecipeByGrid(int gridIndex) {
		if (gridIndex >= GRID_SIZE) throw new IndexOutOfBoundsException(gridIndex);
		return getRecipe(startIndex + gridIndex);
	}

	public boolean isCraftable(RecipeHolder<BioForgingRecipe> recipe) {
		return craftableRecipes.contains(recipe.id());
	}

	public int getTotalItemCountInPlayerInv(ItemStack stack) {
		return itemCounter.getCount(stack);
	}

	public boolean hasSufficientIngredientCount(IngredientStack ingredientStack) {
		return ingredientStack.hasSufficientCount(itemCounter);
	}

	public int getMaxRecipesOnGrid() {
		return Math.min(GRID_SIZE, shownRecipes.size() - startIndex);
	}

	public boolean isSelectedRecipeVisible() {
		int maxIndex = startIndex + GRID_SIZE;
		if (recipeSelection.tab == activeTab) {
			return recipeSelection.index >= startIndex && recipeSelection.index < maxIndex && getGridIndexOfSelectedRecipe() < getMaxRecipesOnGrid();
		}

		if (recipeSelection.recipe != null && (activeTab == 0 || getCurrentCategory() == recipeSelection.recipe.value().getTab())) {
			int maxRecipes = getMaxRecipesOnGrid();
			for (int gridIndex = 0; gridIndex < maxRecipes; gridIndex++) {
				if (getRecipe(startIndex + gridIndex).id().equals(recipeSelection.recipe.id())) {
					crossoverGridIndex = startIndex + gridIndex;
					return true;
				}
			}
		}
		crossoverGridIndex = 0;

		return false;
	}

	public int getGridIndexOfSelectedRecipe() {
		if (recipeSelection.tab == activeTab) {
			return recipeSelection.index - startIndex;
		}

		if (recipeSelection.recipe != null && (activeTab == 0 || getCurrentCategory() == recipeSelection.recipe.value().getTab())) {
			return crossoverGridIndex - startIndex;
		}

		return 0;
	}

	public final boolean hasSelectedRecipe() {
		return getSelectedRecipe() != null;
	}

	@Nullable
	public BioForgingRecipe getSelectedRecipeValue() {
		return recipeSelection.recipe != null ? recipeSelection.recipe.value() : null;
	}

	@Nullable
	public RecipeHolder<BioForgingRecipe> getSelectedRecipe() {
		return recipeSelection.recipe;
	}

	void setSelectedRecipe(int gridIndex) {
		if (gridIndex < 0) {
			recipeSelection = RecipeSelection.EMPTY;
			return;
		}

		int recipeIndex = startIndex + gridIndex;
		RecipeHolder<BioForgingRecipe> recipe = getRecipe(recipeIndex);
		recipeSelection = new RecipeSelection(recipe, activeTab, recipeIndex);

		ModNetworkHandler.sendBioForgeRecipeToServer(menu.containerId, recipe);
	}

	public BioForgeTab getCurrentCategory() {
		return tabs.get(activeTab);
	}

	public int getTabIndex(BioForgeTab category) {
		for (int tabIndex = 0; tabIndex < tabs.size(); tabIndex++) {
			if (tabs.get(tabIndex) == category) return tabIndex;
		}
		return 0;
	}

	public void tick() {
		trackPlayerInvChanges();
	}

	public void trackPlayerInvChanges() {
		if (playerInvChanges != getPlayer().getInventory().getTimesChanged()) {
			countPlayerInvItems();
			playerInvChanges = getPlayer().getInventory().getTimesChanged();
		}
	}

	private void countPlayerInvItems() {
		itemCounter.clear();
		itemCounter.accountStacks(getPlayer().getInventory().getNonEquipmentItems());
		updateAndSearchRecipes();
	}

	public void updateSearchString(String searchString) {
		searchString = searchString.toLowerCase(Locale.ROOT);
		if (searchString.equals(currentSearchString)) return;
		currentSearchString = searchString;
		updateAndSearchRecipes();
	}

	private void reloadRecipes() {
		allRecipes = List.copyOf(RecipeMaps.getClientRecipeMap().byType(ModRecipes.BIO_FORGING_RECIPE_TYPE.get()));

		ClientLevel level = getLevel();
		Item.TooltipContext tooltipContext = Item.TooltipContext.of(level.registryAccess());
		TooltipFlag tooltipFlag = ClientTooltipFlag.of(TooltipFlag.Default.NORMAL);
		searchTree = new FullTextSearchTree<>(
				recipe -> getTooltipLines(getResultItem(recipe), tooltipContext, tooltipFlag),
				recipe -> Stream.of(BuiltInRegistries.ITEM.getKey(getResultItem(recipe).getItem())),
				allRecipes
		);
	}

	private ItemStack getResultItem(RecipeHolder<BioForgingRecipe> recipe) {
		return recipe.value().getResultItem(getLevel().registryAccess());
	}

	private static Stream<String> getTooltipLines(ItemStack stack, Item.TooltipContext context, TooltipFlag flag) {
		return stack.getTooltipLines(context, null, flag).stream()
				.map(line -> ChatFormatting.stripFormatting(line.getString()).trim())
				.filter(line -> !line.isEmpty());
	}

	private Set<ResourceKey<Recipe<?>>> getKnownRecipes(ClientRecipeBook recipeBook) {
		Map<RecipeDisplay, ResourceKey<Recipe<?>>> recipesByDisplay = new HashMap<>();
		for (RecipeHolder<BioForgingRecipe> recipe : allRecipes) {
			for (RecipeDisplay display : recipe.value().display()) {
				recipesByDisplay.putIfAbsent(display, recipe.id());
			}
		}

		Set<ResourceKey<Recipe<?>>> known = new HashSet<>();
		for (RecipeCollection collection : recipeBook.getCollection(ModRecipes.BIO_FORGING_CATEGORY.get())) {
			for (RecipeDisplayEntry entry : collection.getRecipes()) {
				ResourceKey<Recipe<?>> id = recipesByDisplay.get(entry.display());
				if (id != null) known.add(id);
			}
		}
		return known;
	}

	private static boolean isInTab(RecipeHolder<BioForgingRecipe> recipe, BioForgeTab tab) {
		if (tab == ModBioForgeTabs.SEARCH.get()) return true;
		return recipe.value().getTab() == tab;
	}

	private void updateAndSearchRecipes() {
		LocalPlayer player = getPlayer();
		boolean isCreativePlayer = player.isCreative() || !BiomancyConfig.SERVER_SYNCED.doBioForgeRecipeProgression.get();

		ClientRecipeBook recipeBook = player.getRecipeBook();
		Set<ResourceKey<Recipe<?>>> knownRecipes = isCreativePlayer ? Set.of() : getKnownRecipes(recipeBook);
		BioForgeTab currentTab = tabs.get(activeTab);

		craftableRecipes.clear();
		List<RecipeHolder<BioForgingRecipe>> recipes = new ArrayList<>();
		for (RecipeHolder<BioForgingRecipe> recipe : allRecipes) {
			if (!isInTab(recipe, currentTab)) continue;
			if (!isCreativePlayer && !knownRecipes.contains(recipe.id())) continue;

			recipes.add(recipe);
			if (recipe.value().isCraftable(itemCounter)) {
				craftableRecipes.add(recipe.id());
			}
		}

		if (!currentSearchString.isEmpty()) {
			Set<RecipeHolder<BioForgingRecipe>> searchResult = new HashSet<>(searchTree.search(currentSearchString));
			recipes.removeIf(recipe -> !searchResult.contains(recipe));
		}

		if (recipeBook.isFiltering(ModRecipeBookTypes.BIO_FORGE)) {
			recipes.removeIf(recipe -> !craftableRecipes.contains(recipe.id()));
		}

		setShownRecipes(recipes);
	}

	private void setShownRecipes(List<RecipeHolder<BioForgingRecipe>> recipes) {
		shownRecipes = recipes;

		maxPages = Mth.ceil(recipes.size() / (float) GRID_SIZE);
		if (maxPages <= getCurrentPage()) {
			resetPagination();
		}
	}

	public int getTabCount() {
		return tabs.size();
	}

	public BioForgeTab getTab(int tabIndex) {
		return tabs.get(tabIndex);
	}

	public int getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(int tabIndex) {
		activeTab = tabIndex;
		resetPagination();
		updateAndSearchRecipes();
	}

	public boolean isActiveTab(int tabIndex) {
		return tabIndex == activeTab;
	}

	public void onRecipeBookUpdated() {
		reloadRecipes();
		updateAndSearchRecipes();
	}

	record RecipeSelection(@Nullable RecipeHolder<BioForgingRecipe> recipe, int tab, int index) {
		public static RecipeSelection EMPTY = new RecipeSelection(null, -1, -1);

		@Nullable
		public Identifier getRecipeId() {
			return recipe != null ? recipe.id().identifier() : null;
		}
	}

}
