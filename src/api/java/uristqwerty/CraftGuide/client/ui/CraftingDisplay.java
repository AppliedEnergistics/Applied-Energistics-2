package uristqwerty.CraftGuide.client.ui;

import java.util.List;

import uristqwerty.CraftGuide.RecipeCache;
import uristqwerty.CraftGuide.api.CraftGuideRecipe;
import uristqwerty.CraftGuide.api.ItemFilter;
import uristqwerty.CraftGuide.api.Renderer;
import uristqwerty.CraftGuide.client.ui.Rendering.FloatingItemText;
import uristqwerty.CraftGuide.client.ui.Rendering.Overlay;
import uristqwerty.gui_craftguide.rendering.Renderable;

public class CraftingDisplay extends GuiVariableRowHeightGrid implements IRecipeCacheListener
{
	int mouseX;
	int mouseY;
	int mouseRow, mouseRowX, mouseRowY;
	private FloatingItemText itemName = new FloatingItemText("-No Item-");
	private Renderable itemNameOverlay = new Overlay(itemName);
	private RecipeCache recipeCache;
	private CraftGuideRecipe recipeUnderMouse;

	public CraftingDisplay(int x, int y, int width, int height, GuiScrollBar scrollBar, RecipeCache recipeCache)
	{
		super(x, y, width, height, scrollBar, 58, 79);
		flexibleSize = true;

		this.recipeCache = recipeCache;
		recipeCache.addListener(this);
		updateScrollbarSize();
		updateGridSize();
	}

	@Override
	public void draw()
	{
		super.draw();
		drawSelectionName();
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		recipeUnderMouse = null;
		super.mouseMoved(x, y);
	}

	@Override
	public void renderGridCell(GuiRenderer renderer, int xOffset, int yOffset, int cell)
	{
		List<CraftGuideRecipe> recipes = recipeCache.getRecipes();

		if(cell < recipes.size())
		{
			renderRecipe(renderer, xOffset, yOffset, recipes.get(cell));
		}
	}

	private void renderRecipe(Renderer renderer, int xOffset, int yOffset, CraftGuideRecipe recipe)
	{
		if(recipe == recipeUnderMouse)
		{
			recipe.draw(renderer, xOffset, yOffset, true, mouseX, mouseY);
		}
		else
		{
			recipe.draw(renderer, xOffset, yOffset, false, -1, -1);
		}
	}

	public void setFilter(ItemFilter filter)
	{
		recipeCache.filter(filter);
	}

	@Override
	public void onChange(RecipeCache cache)
	{
		updateScrollbarSize();
		updateGridSize();
	}

	private void updateGridSize()
	{
		List<CraftGuideRecipe> recipes = recipeCache.getRecipes();
		int maxWidth = 16;

		for(CraftGuideRecipe recipe: recipes)
		{
			maxWidth = Math.max(maxWidth, recipe.width());
		}

		setColumnWidth(maxWidth);
		recalculateRowHeight();
	}

	private void updateScrollbarSize()
	{
		setCells(recipeCache.getRecipes().size());
	}

	@Override
	public void mouseMovedCell(int cell, int x, int y, boolean inBounds)
	{
		List<CraftGuideRecipe> recipes = recipeCache.getRecipes();

		if(inBounds && cell < recipes.size())
		{
			CraftGuideRecipe recipe = recipes.get(cell);

			if(x < recipe.width() && y < recipe.height())
			{
				mouseX = x;
				mouseY = y;
				recipeUnderMouse = recipe;
			}
		}
	}

	private void drawSelectionName()
	{
		if(recipeUnderMouse != null)
		{
			List<String> text = recipeUnderMouse.getItemText(mouseX, mouseY);

			if(text != null)
			{
				itemName.setText(text);
				render(itemNameOverlay);
			}
		}
	}

	@Override
	public void cellClicked(int cell, int x, int y)
	{
		List<CraftGuideRecipe> recipes = recipeCache.getRecipes();

		if(cell < recipes.size())
		{
			recipeClicked(recipes.get(cell), x, y);
		}
	}

	private void recipeClicked(CraftGuideRecipe recipe, int x, int y)
	{
		ItemFilter stack = recipe.getRecipeClickedResult(x, y);

		if(stack != null)
		{
			setFilter(stack);
		}
	}

	@Override
	public void onReset(RecipeCache cache)
	{
	}

	public ItemFilter getItemFilterForPoint(int x, int y)
	{
		int cell = cellAtCoords(x, y);
		List<CraftGuideRecipe> recipes = recipeCache.getRecipes();

		if(cell < recipes.size())
		{
			return recipes.get(cell).getRecipeClickedResult(x - columnOffset(columnAtX(x)), ((int)scrollBar.getValue() + y) % rowHeight);
		}

		return null;
	}

	@Override
	protected int getMinCellHeight(int cell)
	{
		List<CraftGuideRecipe> recipes = recipeCache.getRecipes();

		if(cell < recipes.size())
		{
			return recipes.get(cell).height();
		}
		else
		{
			return 0;
		}
	}
}
