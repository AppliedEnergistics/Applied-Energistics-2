package uristqwerty.CraftGuide.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uristqwerty.CraftGuide.CraftType;
import uristqwerty.CraftGuide.RecipeCache;
import uristqwerty.CraftGuide.client.ui.Rendering.FloatingItemText;
import uristqwerty.CraftGuide.client.ui.Rendering.Overlay;
import uristqwerty.CraftGuide.client.ui.Rendering.ShadedRect;
import uristqwerty.gui_craftguide.rendering.Renderable;
import uristqwerty.gui_craftguide.rendering.TexturedRect;
import uristqwerty.gui_craftguide.texture.BorderedTexture;
import uristqwerty.gui_craftguide.texture.Texture;

public class CraftTypeDisplay extends GuiScrollableGrid implements IRecipeCacheListener
{
	private Texture displayBackground;
	private Renderable hiddenOverlay = new ShadedRect(-2, -2, 20, 20, 0xc6c6c6, 0x80);
	private RecipeCache recipeCache;
	private Map<CraftType, Integer> settings = new HashMap<CraftType, Integer>();
	private FloatingItemText toolTip = new FloatingItemText("");
	private Overlay toolTipOverlay = new Overlay(toolTip);
	private String toolTipText = "";

	private TexturedRect buttons[] = new TexturedRect[6];

	public CraftTypeDisplay(int x, int y, int width, int height, GuiScrollBar scrollBar, Texture texture, RecipeCache recipeCache)
	{
		super(x, y, width, height, scrollBar, 32, 1);

		displayBackground = new BorderedTexture(texture, 117, 1, 1, 32, 2);

		this.recipeCache = recipeCache;
		recipeCache.addListener(this);
		initTypes(recipeCache);

		buttons[0] = new TexturedRect(0, 0, 28, 28, texture, 113,  76);
		buttons[1] = new TexturedRect(0, 0, 28, 28, texture, 113, 104);
		buttons[2] = new TexturedRect(0, 0, 28, 28, texture, 113, 132);
		buttons[3] = new TexturedRect(0, 0, 28, 28, texture, 141,  76);
		buttons[4] = new TexturedRect(0, 0, 28, 28, texture, 141, 104);
		buttons[5] = new TexturedRect(0, 0, 28, 28, texture, 141, 132);

		setRows(recipeCache.getCraftTypes().size());
		setCells(recipeCache.getCraftTypes().size());
	}

	private void initTypes(RecipeCache recipeCache)
	{
		Set<CraftType> types = recipeCache.getCraftTypes();
		Set<CraftType> filteredTypes = recipeCache.getFilterTypes();

		if(filteredTypes != null)
		{
			for(CraftType type: types)
			{
				settings.put(type, 1);
			}

			for(CraftType type: filteredTypes)
			{
				settings.put(type, 0);
			}
		}
	}

	@Override
	public void renderGridRow(GuiRenderer renderer, int xOffset, int yOffset, int row)
	{
		Set<CraftType> types = recipeCache.getCraftTypes();

		if(row < types.size())
		{
			CraftType type = (CraftType)types.toArray()[row];
			displayBackground.renderRect(renderer, xOffset, yOffset, width(), rowHeight, 0, 0);
			renderer.drawItemStack(type.getDisplayStack(), xOffset + 8, yOffset + 8, false);

			if(hidden(type))
			{
				hiddenOverlay.render(renderer, xOffset + 8, yOffset + 8);
			}

			for(int i = 0; i < 3; i++)
			{
				TexturedRect rect = buttons[i == setting(type)? i + 3 : i];

				rect.render(renderer, xOffset + i * 29 + (bounds.width() - (3 * 29 + 24)) / 2 + 24, yOffset + 2);
			}
		}
	}

	private boolean hidden(CraftType type)
	{
		switch(setting(type))
		{
			case 2:
				return false;

			case 1:
				return true;

			default:
				for(CraftType otherType: settings.keySet())
				{
					if(setting(otherType) == 2)
					{
						return true;
					}
				}

				return false;
		}
	}

	@Override
	public void rowClicked(int row, int x, int y, boolean inBounds)
	{
		if(y > 1 && y < 30 && inBounds)
		{
			if(x >= (bounds.width() - (3 * 29 + 24)) / 2 + 24 && x < (bounds.width() - (3 * 29 + 24)) / 2 + 24 + 3 * 29)
			{
				int relX = (x - ((bounds.width() - (3 * 29 + 24)) / 2 + 24));

				if(relX % 29 != 28)
				{
					set(row, relX / 29);
				}
			}
		}
	}

	@Override
	public void onChange(RecipeCache cache)
	{
		setRows(recipeCache.getCraftTypes().size());
	}

	@Override
	public void setColumns(int columns)
	{
		super.setColumns(1);
	}

	private int setting(CraftType type)
	{
		if(!settings.containsKey(type))
		{
			settings.put(type, 0);
		}

		return settings.get(type);
	}

	private void set(int row, int setting)
	{
		Set<CraftType> types = recipeCache.getCraftTypes();

		if(row < types.size())
		{
			CraftType type = (CraftType)types.toArray()[row];

			settings.put(type, setting);

			settingChanged(type, setting);
		}
	}

	private void settingChanged(CraftType type, int setting)
	{
		if(setting == 2)
		{
			for(CraftType settingType: settings.keySet())
			{
				if(settingType != type && settings.get(settingType) == 2)
				{
					settings.put(settingType, 0);
				}
			}
		}

		updateFilter();
	}

	private void updateFilter()
	{
		Set<CraftType> set = new HashSet<CraftType>();

		for(CraftType type: settings.keySet())
		{
			if(setting(type) == 2)
			{
				set.add(type);
				recipeCache.setTypes(set);
				return;
			}
		}

		for(CraftType type: recipeCache.getCraftTypes())
		{
			if(setting(type) != 1)
			{
				set.add(type);
			}
		}

		recipeCache.setTypes(set);
	}

	@Override
	public void onReset(RecipeCache cache)
	{
	}

	@Override
	public void draw()
	{
		super.draw();
		if(toolTipText != "")
		{
			toolTip.setText(toolTipText);
			render(toolTipOverlay);
		}
	}

	@Override
	public void mouseMovedRow(int row, int x, int y, boolean inBounds)
	{
		super.mouseMovedRow(row, x, y, inBounds);

		if(row < recipeCache.getCraftTypes().size() && y > 1 && y < 30 && inBounds)
		{
			if(x >= (bounds.width() - (3 * 29 + 24)) / 2 + 24 && x < (bounds.width() - (3 * 29 + 24)) / 2 + 24 + 3 * 29)
			{
				int relX = (x - ((bounds.width() - (3 * 29 + 24)) / 2 + 24));

				if(relX % 29 != 28)
				{
					switch(relX / 29)
					{
						case 0:
							toolTipText = "Show recipes of this type";
							break;

						case 1:
							toolTipText = "Hide recipes of this type";
							break;

						case 2:
							toolTipText = "Show only recipes of this type";
					}
				}
			}
		}
	}

	@Override
	public void mouseMoved(int x, int y)
	{
		toolTipText = "";
		super.mouseMoved(x, y);
	}

	@Override
	protected int getMinCellHeight(int i)
	{
		return 32;
	}
}
