package com.github.elenterius.biomancy.client.gui;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.github.elenterius.biomancy.BiomancyMod;
import com.github.elenterius.biomancy.client.gui.component.CustomEditBox;
import com.github.elenterius.biomancy.client.gui.tooltip.ScreenNutrientFuelConsumer;
import com.github.elenterius.biomancy.client.gui.tooltip.ScreenTooltipStyleProvider;
import com.github.elenterius.biomancy.client.util.GuiRenderUtil;
import com.github.elenterius.biomancy.client.util.GuiUtil;
import com.github.elenterius.biomancy.crafting.IngredientStack;
import com.github.elenterius.biomancy.crafting.recipe.BioForgingRecipe;
import com.github.elenterius.biomancy.init.ModSoundEvents;
import com.github.elenterius.biomancy.menu.BioForgeMenu;
import com.github.elenterius.biomancy.sounds.SoundUtil;
import com.github.elenterius.biomancy.styles.ColorStyles;
import com.github.elenterius.biomancy.util.ComponentUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BioForgeScreen extends AbstractContainerScreen<BioForgeMenu> implements ScreenTooltipStyleProvider, ScreenNutrientFuelConsumer {

	private static final Identifier BACKGROUND_TEXTURE = BiomancyMod.rl("textures/gui/menu_bio_forge.png");
	private final TabsHelper tabsHelper = new TabsHelper();
	private final IngredientsHelper ingredientsHelper = new IngredientsHelper();
	private BioForgeScreenController recipeBook;
	private CustomEditBox searchInput;
	private boolean ignoreTextInput;

	public BioForgeScreen(BioForgeMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 292, 219);
	}

	@Override
	protected void init() {
		super.init();

		searchInput = new CustomEditBox(Objects.requireNonNull(minecraft).font, leftPos + 26, topPos + 16, 78, 14, ComponentUtil.translatable("itemGroup.search"));
		searchInput.setMaxLength(50);
		searchInput.setTextHint(ComponentUtil.translatable("gui.recipebook.search_hint").withStyle(Style.EMPTY.withItalic(true).withColor(ColorStyles.TEXT_ACCENT_FORGE_DARK)));
		searchInput.setTextColor(ColorStyles.TEXT_ACCENT_FORGE);

		recipeBook = new BioForgeScreenController(minecraft, menu);
	}

	public void onRecipeBookUpdated() {
		recipeBook.onRecipeBookUpdated();
	}

	@Override
	protected void containerTick() {
		recipeBook.tick();
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type) {
		super.slotClicked(slot, slotId, mouseButton, type);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (searchInput.mouseClicked(event, doubleClick)) {
			searchInput.setFocused(true);
			return true;
		}
		else {
			searchInput.setFocused(false);
		}

		if (recipeBook.hasRecipesOnPage()) {
			int recipes = recipeBook.getMaxRecipesOnGrid();
			for (int i = 0; i < recipes; i++) {
				int pX = leftPos + 13 + (20 + 5 - 2) * (i % BioForgeScreenController.COLS);
				int pY = topPos + 37 + (20 + 5 - 2) * (i / BioForgeScreenController.COLS);
				if (GuiUtil.isInRect(pX, pY, 24, 24, mouseX, mouseY)) {
					SoundUtil.Client.playUISound(ModSoundEvents.UI_BIO_FORGE_SELECT_RECIPE);
					recipeBook.setSelectedRecipe(i);
					return true;
				}
			}

			if (recipeBook.getMaxPages() > 1) {
				int x = leftPos + 60 + 1;
				int y = topPos + 211 - font.lineHeight * 2 - 2;
				if (recipeBook.hasPrevPage() && GuiUtil.isInRect(x - 22 - 8 - 1, y, 8, 13, mouseX, mouseY)) {
					SoundUtil.Client.playUISound(ModSoundEvents.UI_BUTTON_CLICK);
					recipeBook.goToPrevPage();
					return true;
				}
				if (recipeBook.hasNextPage() && GuiUtil.isInRect(x + 22, y, 8, 13, mouseX, mouseY)) {
					SoundUtil.Client.playUISound(ModSoundEvents.UI_BUTTON_CLICK);
					recipeBook.goToNextPage();
					return true;
				}
			}
		}

		if (tabsHelper.clickMouse(mouseX, mouseY)) return true;

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		int scanCode = event.scancode();
		int modifiers = event.modifiers();
		ignoreTextInput = false;

		if (searchInput.keyPressed(event)) {
			recipeBook.updateSearchString(searchInput.getValue().toLowerCase(Locale.ROOT));
			return true;
		}

		if (searchInput.isFocused() && searchInput.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
			return true;
		}

		if (minecraft.options.keyChat.matches(event) && !searchInput.isFocused()) {
			ignoreTextInput = true;
			searchInput.setFocused(true);
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		int keyCode = event.key();
		int scanCode = event.scancode();
		int modifiers = event.modifiers();
		ignoreTextInput = false;
		return super.keyReleased(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		char codePoint = (char) event.codepoint();
		if (ignoreTextInput) return false;

		if (searchInput.charTyped(event)) {
			recipeBook.updateSearchString(searchInput.getValue().toLowerCase(Locale.ROOT));
			return true;
		}

		return super.charTyped(event);
	}

	public void blit(GuiGraphicsExtractor guiGraphics, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, uOffset, vOffset, uWidth, vHeight, 512, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
	}

	private float time;

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (!Minecraft.getInstance().hasControlDown()) {
			time += partialTick;
		}

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		blit(guiGraphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		renderFuelBar(guiGraphics);
		tabsHelper.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		ingredientsHelper.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		renderRecipeResult(guiGraphics);
		renderRecipeSelectionGrid(guiGraphics);

		searchInput.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	private void renderRecipeResult(GuiGraphicsExtractor guiGraphics) {
		BioForgingRecipe selectedRecipe = recipeBook.getSelectedRecipeValue();
		if (selectedRecipe != null && menu.isResultEmpty()) {
			ItemStack stack = selectedRecipe.getResultItem(minecraft.level.registryAccess());
			int x = leftPos + 194 + 2;
			int y = topPos + 33 + 2;
			guiGraphics.fill(x, y, x + 16, y + 16, 0x30_ff0000);
			guiGraphics.fakeItem(stack, x, y);
			guiGraphics.fill(x, y, x + 16, y + 16, 0x30_ffffff);
			guiGraphics.itemDecorations(font, stack, x, y);
		}
	}

	private void renderRecipeSelectionGrid(GuiGraphicsExtractor guiGraphics) {
		if (!recipeBook.hasRecipesOnPage()) return;

		if (recipeBook.hasSelectedRecipe() && recipeBook.isSelectedRecipeVisible()) {
			int gridIndex = recipeBook.getGridIndexOfSelectedRecipe();
			RecipeHolder<BioForgingRecipe> recipe = recipeBook.getRecipeByGrid(gridIndex);
			boolean isCraftable = recipeBook.isCraftable(recipe);
			renderTileSelection(guiGraphics, gridIndex, isCraftable);
		}

		int maxRecipes = recipeBook.getMaxRecipesOnGrid();
		for (int gridIndex = 0; gridIndex < maxRecipes; gridIndex++) {
			RecipeHolder<BioForgingRecipe> recipe = recipeBook.getRecipeByGrid(gridIndex);
			boolean isCraftable = recipeBook.isCraftable(recipe);
			renderRecipeTile(guiGraphics, gridIndex, isCraftable, recipe.value().getResultItem(minecraft.level.registryAccess()));
		}

		renderPagination(guiGraphics);
	}

	private void renderPagination(GuiGraphicsExtractor guiGraphics) {
		if (recipeBook.getMaxPages() < 2) return;

		int x = leftPos + 60 + 1;
		int y = topPos + 211 - font.lineHeight * 2 - 2;
		int currentPage = recipeBook.getCurrentPage();

		if (currentPage > 1) blit(guiGraphics, x - 22 - 8 - 1, y, 298, 58, 8, 13);
		if (currentPage < recipeBook.getMaxPages()) blit(guiGraphics, x + 22, y, 334, 58, 8, 13);
		String text = "%d/%d".formatted(currentPage, recipeBook.getMaxPages());
		guiGraphics.text(font, text, (int) (x - font.width(text) / 2f), y + 3, ColorStyles.TEXT_ACCENT_FORGE);
	}

	private void renderRecipeTile(GuiGraphicsExtractor guiGraphics, int idx, boolean isCraftable, ItemStack stack) {
		int x = leftPos + 12 + (20 + 5) * (idx % BioForgeScreenController.COLS);
		int y = topPos + 36 + (20 + 5) * (idx / BioForgeScreenController.COLS);
		renderRecipeTile(guiGraphics, x, y, stack, !isCraftable);
	}

	private void renderRecipeTile(GuiGraphicsExtractor guiGraphics, int x, int y, ItemStack stack, boolean redOverlay) {
		blit(guiGraphics, x, y, 295, 30, 22, 22);

		if (redOverlay) {
			blit(guiGraphics, x, y, 323, 30, 22, 22);
			guiGraphics.fill(x, y, x + 22, y + 22, 0x40_00_00_00 | ColorStyles.TEXT_ERROR);
		}
		guiGraphics.item(stack, x + 3, y + 3);
	}

	private void renderTileSelection(GuiGraphicsExtractor guiGraphics, int idx, boolean isValid) {
		int x = leftPos + 12 + 25 * (idx % BioForgeScreenController.COLS) - 1;
		int y = topPos + 36 + 25 * (idx / BioForgeScreenController.COLS) - 1;
		blit(guiGraphics, x, y, isValid ? 295 : 321, 2, 24, 24);
	}

	private void renderFuelBar(GuiGraphicsExtractor guiGraphics) {
		float fuelPct = menu.getFuelAmountNormalized();
		int vHeight = (int) (fuelPct * 36) + (fuelPct > 0 ? 1 : 0);
		blit(guiGraphics, leftPos + 144, topPos + 13 + 36 - vHeight, 353, 9 + 36 - vHeight, 5, vHeight);

		BioForgingRecipe selectedRecipe = recipeBook.getSelectedRecipeValue();
		int cost = selectedRecipe != null ? selectedRecipe.getCraftingCostNutrients() : 0;
		if (cost <= menu.getFuelAmount()) return;

		int x = leftPos + 144;
		int y = topPos + 13 + 36 - ((int) ((cost / (float) menu.getMaxFuelAmount()) * 36) + 1);
		guiGraphics.fill(x - 3, y, x + 5 + 3, y + 1, 0x30_ff0000);
		guiGraphics.fill(x + 5 + 3, y, x + 5 + 3 + 1, y + 1, 0xff_ff0000);
		guiGraphics.fill(x + 5 + 3 + 1, y - 1, x + 5 + 3 + 2, y + 2, 0xff_ff0000);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (menu.getCarried().isEmpty()) {
			if (tabsHelper.renderTooltip(guiGraphics, mouseX, mouseY)) return;
			if (renderFuelTooltip(guiGraphics, mouseX, mouseY)) return;
			if (renderRecipeSelectionGridTooltip(guiGraphics, mouseX, mouseY)) return;
			if (ingredientsHelper.renderTooltip(guiGraphics, mouseX, mouseY)) return;
		}
		super.extractTooltip(guiGraphics, mouseX, mouseY);
	}

	private boolean renderRecipeSelectionGridTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (recipeBook.hasRecipesOnPage()) {
			int maxIndex = recipeBook.getMaxRecipesOnGrid();

			int minX = leftPos + 13;
			int minY = topPos + 37;
			int maxX = leftPos + 13 + 25 * BioForgeScreenController.COLS - 5;
			int maxY = topPos + 37 + 25 * Mth.ceil(maxIndex / (float) BioForgeScreenController.COLS) - 5;
			if (!GuiUtil.isInRectAB(minX, minY, maxX, maxY, mouseX, mouseY)) return false;

			for (int index = 0; index < maxIndex; index++) {
				int x = leftPos + 13 + 25 * (index % BioForgeScreenController.COLS);
				int y = topPos + 37 + 25 * (index / BioForgeScreenController.COLS);
				if (GuiUtil.isInRect(x, y, 20, 20, mouseX, mouseY)) {
					guiGraphics.setTooltipForNextFrame(font, recipeBook.getRecipeByGrid(index).value().getResultItem(minecraft.level.registryAccess()), mouseX, mouseY);
					return true;
				}
			}
		}
		return false;
	}

	private boolean renderFuelTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!GuiUtil.isInRect(leftPos + 144, topPos + 13, 5, 36, mouseX, mouseY)) return false;

		int maxFuel = menu.getMaxFuelAmount();
		int fuelAmount = menu.getFuelAmount();

		BioForgingRecipe selectedRecipe = recipeBook.getSelectedRecipeValue();
		int totalFuelCost = selectedRecipe != null ? selectedRecipe.getCraftingCostNutrients() : 0;

		GuiRenderUtil.drawFuelTooltip(font, guiGraphics, mouseX, mouseY, maxFuel, fuelAmount, totalFuelCost);
		return true;
	}

	private final class IngredientsHelper implements Renderable {

		private static final int X_OFFSET = 141 + 3;
		private static final int Y_OFFSET = 82 + 3;

		@Override
		public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
			BioForgingRecipe recipe = recipeBook.getSelectedRecipeValue();
			if (recipe == null) return;

			int x = leftPos + X_OFFSET;
			int y = topPos + Y_OFFSET;

			List<IngredientStack> ingredients = recipe.getIngredientQuantities();
			for (int i = 0; i < ingredients.size(); i++) {
				IngredientStack ingredientStack = ingredients.get(i);
				ItemStack[] items = ingredientStack.getItems();
				ItemStack itemStack = items[Mth.floor(time / 30f) % items.length];

				boolean isSufficientCount = recipeBook.hasSufficientIngredientCount(ingredientStack);

				renderItemWithQuantity(guiGraphics, itemStack, isSufficientCount, ingredientStack.count(), x + 26 * i, y);
			}
		}

		private void renderItemWithQuantity(GuiGraphicsExtractor guiGraphics, ItemStack stack, boolean isSufficientCount, int requiredCount, int x, int y) {
			blit(guiGraphics, x - 3, y - 3, isSufficientCount ? 330 : 354, 74, 22, 22);
			guiGraphics.item(stack, x, y);
			String text = "x" + requiredCount;
			guiGraphics.text(font, text, x + 16 + 4 - font.width(text), y + 16 + 4 + 1, isSufficientCount ? ColorStyles.TEXT_SUCCESS : ColorStyles.TEXT_ERROR);
		}

		private boolean renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			BioForgingRecipe selectedRecipe = recipeBook.getSelectedRecipeValue();
			if (selectedRecipe == null) return false;

			List<IngredientStack> ingredients = selectedRecipe.getIngredientQuantities();
			for (int i = 0; i < ingredients.size(); i++) {
				int x = leftPos + X_OFFSET + 26 * i;
				int y = topPos + Y_OFFSET;

				if (GuiUtil.isInRect(x, y, 20, 20, mouseX, mouseY)) {
					ItemStack[] items = ingredients.get(i).getItems();
					ItemStack itemStack = items[Mth.floor(time / 30f) % items.length];

					guiGraphics.setTooltipForNextFrame(font, itemStack, mouseX, mouseY);
					return true;
				}
			}
			return false;
		}

	}

	private final class TabsHelper implements Renderable {
		private static final int WIDTH = 24;
		private static final int HEIGHT = 32;
		private static final int Y_OFFSET = 7;

		private boolean clickMouse(double mouseX, double mouseY) {
			for (int i = 0; i < recipeBook.getTabCount(); i++) {
				int pX = leftPos - WIDTH;
				int pY = topPos + Y_OFFSET + HEIGHT * i;
				if (!recipeBook.isActiveTab(i) && GuiUtil.isInRect(pX, pY + 3, WIDTH, HEIGHT - 3, mouseX, mouseY)) {
					SoundUtil.Client.playUISound(ModSoundEvents.UI_BUTTON_CLICK);
					recipeBook.setActiveTab(i);
					return true;
				}
			}

			return false;
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
			for (int index = 0; index < recipeBook.getTabCount(); index++) {
				renderTab(guiGraphics, index, recipeBook.isActiveTab(index), recipeBook.getTab(index).getIcon());
			}
		}

		private void renderTab(GuiGraphicsExtractor guiGraphics, int index, boolean isActive, ItemStack stack) {
			int x = leftPos - WIDTH + 2;
			int y = topPos + Y_OFFSET + HEIGHT * index;
			blit(guiGraphics, x, y, 297, isActive ? 114 : 78, WIDTH, HEIGHT);
			guiGraphics.item(stack, x + 5, y + 8);
		}

		private boolean renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			int minX = leftPos - WIDTH + 2;
			int minY = topPos + Y_OFFSET;
			int maxX = leftPos;
			int maxY = topPos + Y_OFFSET + HEIGHT * recipeBook.getTabCount();

			if (!GuiUtil.isInRectAB(minX, minY, maxX, maxY, mouseX, mouseY)) return false;

			for (int index = 0; index < recipeBook.getTabCount(); index++) {
				int x = leftPos - WIDTH + 2;
				int y = topPos + Y_OFFSET + HEIGHT * index;
				if (GuiUtil.isInRect(x, y, WIDTH, HEIGHT, mouseX, mouseY)) {
					guiGraphics.setTooltipForNextFrame(font, ComponentUtil.translatable(recipeBook.getTab(index).translationKey()), mouseX, mouseY);
					return true;
				}
			}

			return false;
		}

	}

}
