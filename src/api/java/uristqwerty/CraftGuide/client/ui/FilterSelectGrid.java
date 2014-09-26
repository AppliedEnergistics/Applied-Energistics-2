package uristqwerty.CraftGuide.client.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import uristqwerty.CraftGuide.CommonUtilities;
import uristqwerty.CraftGuide.CraftGuide;
import uristqwerty.CraftGuide.CraftType;
import uristqwerty.CraftGuide.RecipeCache;
import uristqwerty.CraftGuide.api.NamedTexture;
import uristqwerty.CraftGuide.api.Util;
import uristqwerty.CraftGuide.client.ui.Rendering.FloatingItemText;
import uristqwerty.CraftGuide.client.ui.Rendering.Overlay;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.rendering.TexturedRect;
import uristqwerty.gui_craftguide.texture.Texture;

public class FilterSelectGrid extends GuiScrollableGrid implements IRecipeCacheListener, ITextInputListener
{
	private GuiButton backButton;
	private GuiTabbedDisplay display;
	private RecipeCache recipeCache;
	private Renderable gridBackground;
	private Object[] items;
	private List<Object> itemResults = new ArrayList<Object>();
	private FloatingItemText itemName = new FloatingItemText("-No Item-");
	private Renderable itemNameOverlay = new Overlay(itemName);
	private boolean overItem = false;
	private String searchText = "";

	private int lastMouseX, lastMouseY;

	private NamedTexture textImage = Util.instance.getTexture("TextFilter");
	private NamedTexture overlayAny = Util.instance.getTexture("ItemStack-Any");
	private NamedTexture overlayForge = Util.instance.getTexture("ItemStack-OreDict");

	public FilterSelectGrid(int x, int y, int width, int height, GuiScrollBar scrollBar, Texture texture,
		RecipeCache recipeCache, GuiButton backButton, GuiTabbedDisplay display)
	{
		super(x, y, width, height, scrollBar, 18, 18);
		flexibleSize = true;

		this.backButton = backButton;
		this.display = display;
		this.recipeCache = recipeCache;
		recipeCache.addListener(this);
		setColumns();
		onReset(recipeCache);

		gridBackground = new TexturedRect(0, 0, 18, 18, texture, 238, 219);
	}

	@Override
	public void cellClicked(int cell, int x, int y)
	{
		if(cell < itemResults.size())
		{
			recipeCache.filter(Util.instance.getCommonFilter(itemResults.get(cell)));
			display.openTab(backButton);
		}
		else if(cell == itemResults.size() && searchText != null && !searchText.isEmpty())
		{
			recipeCache.filter(Util.instance.getCommonFilter(searchText));
			display.openTab(backButton);
		}
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		overItem = false;
		lastMouseX = x;
		lastMouseY = y;
		super.mouseMoved(x, y);
	}

	@Override
	public void mouseMovedCell(int cell, int x, int y, boolean inBounds)
	{
		if(inBounds)
		{
			if(cell < itemResults.size())
			{
				overItem = true;
				itemName.setText(CommonUtilities.getExtendedItemStackText(itemResults.get(cell)));
			}
			else if(cell == itemResults.size() && searchText != null && !searchText.isEmpty())
			{
				overItem = true;
				itemName.setText("\u00a77Text search: '" + searchText + "'");
			}
		}
	}

	@Override
	public void renderGridCell(GuiRenderer renderer, int xOffset, int yOffset, int cell)
	{
		if(cell < itemResults.size())
		{
			gridBackground.render(renderer, xOffset, yOffset);
			ItemStack stack = displayItem(cell);

			renderer.drawItemStack(stack, xOffset + 1, yOffset + 1);

			if(CommonUtilities.getItemDamage(stack) == CraftGuide.DAMAGE_WILDCARD)
			{
				renderer.renderRect(xOffset, yOffset, 18, 18, overlayAny);
			}

			if(itemResults.get(cell) instanceof ArrayList)
			{
				renderer.renderRect(xOffset, yOffset, 18, 18, overlayForge);
			}
		}
		else if(cell == itemResults.size() && searchText != null && !searchText.isEmpty())
		{
			gridBackground.render(renderer, xOffset, yOffset);
			renderer.renderRect(xOffset + 1, yOffset + 1, 16, 16, textImage);
		}
	}

	private ItemStack displayItem(int cell)
	{
		Object item = itemResults.get(cell);

		if(item instanceof ItemStack)
		{
			return (ItemStack)item;
		}
		else if(item instanceof ArrayList && ((ArrayList)item).size() > 0)
		{
			return (ItemStack)((ArrayList)item).get(0);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void draw()
	{
		super.draw();

		if(overItem)
		{
			render(itemNameOverlay);
		}
	}

	@Override
	public void onReset(RecipeCache cache)
	{
		items = cache.getAllItems().toArray();
		search(searchText);
	}

	public void search(String text)
	{
		searchText = text;
		itemResults.clear();

		if(text == null || text.isEmpty())
		{
			for(Object item: items)
			{
				itemResults.add(((CraftType)item).getStack());
			}

			setCells(itemResults.size());
		}
		else
		{
			String search = text.toLowerCase();

			for(Object item: items)
			{
				Object stack = ((CraftType)item).getStack();

				if(CommonUtilities.searchExtendedItemStackText(stack, search))
				{
					itemResults.add(stack);
				}
			}

			setCells(itemResults.size() + 1);
		}

		mouseMoved(lastMouseX, lastMouseY);
	}


	@Override
	public void onChange(RecipeCache cache)
	{
	}

	@Override
	public void onSubmit(GuiTextInput input)
	{
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || CraftGuide.textSearchRequiresShift == false)
		{
			recipeCache.filter(Util.instance.getCommonFilter(input.getText()));
			display.openTab(backButton);
		}
	}

	@Override
	public void onTextChanged(GuiTextInput input)
	{
		search(input.getText());
	}

	public ItemStack stackAtCoords(int x, int y)
	{
		int cell = cellAtCoords(x, y);

		if(cell < itemResults.size())
		{
			Object content = itemResults.get(cell);

			if(content instanceof ItemStack)
			{
				return (ItemStack)content;
			}
			else if(content instanceof List && ((List)content).size() > 0 && ((List)content).get(0) instanceof ItemStack)
			{
				return (ItemStack)((List)content).get(0);
			}
		}

		return null;
	}

	@Override
	protected int getMinCellHeight(int i)
	{
		return 18;
	}
}
