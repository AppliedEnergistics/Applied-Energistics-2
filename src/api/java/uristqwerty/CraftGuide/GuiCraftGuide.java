package uristqwerty.CraftGuide;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.CraftGuide.api.Util;
import uristqwerty.CraftGuide.client.FilterDisplay;
import uristqwerty.CraftGuide.client.ui.ButtonTemplate;
import uristqwerty.CraftGuide.client.ui.CraftTypeDisplay;
import uristqwerty.CraftGuide.client.ui.CraftingDisplay;
import uristqwerty.CraftGuide.client.ui.FilterSelectGrid;
import uristqwerty.CraftGuide.client.ui.GuiButton;
import uristqwerty.CraftGuide.client.ui.GuiButton.ButtonState;
import uristqwerty.CraftGuide.client.ui.GuiResizeHandle;
import uristqwerty.CraftGuide.client.ui.GuiScrollBar;
import uristqwerty.CraftGuide.client.ui.GuiSlider;
import uristqwerty.CraftGuide.client.ui.GuiTabbedDisplay;
import uristqwerty.CraftGuide.client.ui.GuiText;
import uristqwerty.CraftGuide.client.ui.GuiTextInput;
import uristqwerty.CraftGuide.client.ui.IButtonListener;
import uristqwerty.CraftGuide.client.ui.RowCount;
import uristqwerty.CraftGuide.client.ui.ToggleButton;
import uristqwerty.gui_craftguide.components.GuiElement;
import uristqwerty.gui_craftguide.components.GuiElement.AnchorPoint;
import uristqwerty.gui_craftguide.components.Image;
import uristqwerty.gui_craftguide.components.Window;
import uristqwerty.gui_craftguide.minecraft.Gui;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.texture.TextureClip;

public class GuiCraftGuide extends Gui
{
	private RecipeCache recipeCache = new RecipeCache();
	private FilterDisplay filter;
	private CraftingDisplay craftingDisplay;
	private Window guiOverlay;

	private static GuiCraftGuide instance;

	private Texture paneBackground = DynamicTexture.instance("pane");
	private Texture windowBackground = DynamicTexture.instance("window");

	public static Map<SlotType, Boolean> filterSlotTypes = new EnumMap<SlotType, Boolean>(SlotType.class);

	static
	{
		filterSlotTypes.put(SlotType.INPUT_SLOT, true);
		filterSlotTypes.put(SlotType.OUTPUT_SLOT, true);
		filterSlotTypes.put(SlotType.MACHINE_SLOT, false);
	}

	public static GuiCraftGuide getInstance()
	{
		if(instance == null)
		{
			instance = new GuiCraftGuide();
		}

		return instance;
	}

	public void setFilterItem(ItemStack item)
	{
		try
		{
			recipeCache.filter(Util.instance.getCommonFilter(item.copy()));
		}
		catch(Exception e)
		{
			CraftGuideLog.log(e, "Error while trying to filter displayed recipes.", true);
		}
	}

	private static final int initialWindowWidth = 248;
	private static final int initialWindowHeight = 198;

	public GuiCraftGuide()
	{
		super(initialWindowWidth, initialWindowHeight);

		Texture texture = DynamicTexture.instance("base_image");

		ButtonTemplate buttonTemplate = new ButtonTemplate(
				new Texture[]{
						DynamicTexture.instance("button_up"),
						DynamicTexture.instance("button_over"),
						DynamicTexture.instance("button_up"),
						DynamicTexture.instance("button_down"),
				});

		guiWindow.background = windowBackground;

		guiWindow.addElement(
			new GuiResizeHandle(
				initialWindowWidth - 8, initialWindowHeight - 8, 8, 8,
				guiWindow
			));

		guiWindow.addElement(
			new GuiButton(initialWindowWidth - 8, initialWindowHeight - 8, 8, 8, texture, 0, 191)
				.anchor(AnchorPoint.BOTTOM_RIGHT));

		guiWindow.addElement(
				new GuiElement(5, 5, 58, 86)
					.setBackground(paneBackground));

		guiWindow.addElement(
			new GuiTabbedDisplay(0, 0, initialWindowWidth, initialWindowHeight)
				.addTab(
					generateRecipeTab(texture, buttonTemplate)
						.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT),
					(GuiButton)new GuiButton(6, 6, 28, 28, buttonTemplate)
						.setToolTip("Recipe list")
						.addElement(new GuiElement(0, 0, 28,28)
							.setBackground(new TextureClip(texture, 1, 76, 28,28))
							.setClickable(false)))
				.addTab(
					generateTypeTab(texture, buttonTemplate)
						.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT),
					(GuiButton)new GuiButton(34, 6, 28, 28, buttonTemplate)
						.setToolTip("Show/Hide recipes by crafting type")
						.addElement(new GuiElement(0, 0, 28,28)
							.setBackground(new TextureClip(texture, 29, 76, 28,28))
							.setClickable(false)))
				.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
				.setClickable(false));


		guiOverlay = new Window(0, 0, 0, 0, renderer);

		/*guiOverlay.addElement(
			new GuiBorderedRect(
				0, 0, 0, 0,
				texture, 78, 1, 2, 32
			)
			.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
		);*/
	}

	private GuiElement generateRecipeTab(Texture texture, ButtonTemplate buttonTemplate)
	{
		GuiElement recipeTab = new GuiElement(0, 0, initialWindowWidth, initialWindowHeight)
				.setClickable(false);

		ButtonTemplate toggleTemplate = new ButtonTemplate(
				new Texture[]{
						DynamicTexture.instance("toggle_off"),
						DynamicTexture.instance("toggle_off_over"),
						DynamicTexture.instance("toggle_on"),
						DynamicTexture.instance("toggle_on_over"),
				});

		recipeTab.addElement(
				new GuiElement(initialWindowWidth - 19, 5, 14, initialWindowHeight - 10)
					.setBackground(paneBackground)
					.anchor(AnchorPoint.TOP_RIGHT, AnchorPoint.BOTTOM_RIGHT));


		recipeTab.addElement(
				new ToggleButton(8, 100, 13, 13, toggleTemplate)
					.setState(filterSlotTypes.get(SlotType.INPUT_SLOT)? ButtonState.DOWN : ButtonState.UP)
					.addButtonListener(new FilterToggle(SlotType.INPUT_SLOT))
					.anchor(AnchorPoint.BOTTOM_LEFT)
					.addElement(
							new GuiText(15, 3, CraftGuide.getTranslation("filter_type.input"), 0xff000000)));


		recipeTab.addElement(
				new ToggleButton(8, 115, 13, 13, toggleTemplate)
					.setState(filterSlotTypes.get(SlotType.OUTPUT_SLOT)? ButtonState.DOWN : ButtonState.UP)
					.addButtonListener(new FilterToggle(SlotType.OUTPUT_SLOT))
					.anchor(AnchorPoint.BOTTOM_LEFT)
					.addElement(
							new GuiText(15, 3, CraftGuide.getTranslation("filter_type.output"), 0xff000000)));

		recipeTab.addElement(
				new ToggleButton(8, 130, 13, 13, toggleTemplate)
					.setState(filterSlotTypes.get(SlotType.MACHINE_SLOT)? ButtonState.DOWN : ButtonState.UP)
					.addButtonListener(new FilterToggle(SlotType.MACHINE_SLOT))
					.anchor(AnchorPoint.BOTTOM_LEFT)
					.addElement(
							new GuiText(15, 3, CraftGuide.getTranslation("filter_type.machine"), 0xff000000)));

		GuiButton clearButton =
			(GuiButton) new GuiButton(8, initialWindowHeight - 18, 50, 13, buttonTemplate, "Clear")
				.anchor(AnchorPoint.BOTTOM_LEFT);

		recipeTab.addElement(clearButton);

		recipeTab.addElement(
			new GuiText(9, 151, CraftGuide.getTranslation("filter"), 0xff000000)
				.anchor(AnchorPoint.BOTTOM_LEFT));

		recipeTab.addElement(
			new Image(40, 146, 18, 18, texture, 238, 219)
				.anchor(AnchorPoint.BOTTOM_LEFT));

		filter = new FilterDisplay(41, 147);
		filter.anchor(AnchorPoint.BOTTOM_LEFT);
		recipeTab.addElement(filter);

		GuiElement recipeArea = new GuiElement(0, 0, initialWindowWidth, initialWindowHeight)
			.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
			.setClickable(false);

		GuiElement itemListArea = new GuiElement(0, 0, initialWindowWidth, initialWindowHeight)
			.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
			.setClickable(false);

		GuiButton backButton =
			(GuiButton) new GuiButton(8, 166, 50, 13, buttonTemplate, "Back")
				.anchor(AnchorPoint.BOTTOM_LEFT);

		GuiButton itemListButton =
			(GuiButton) new GuiButton(8, 166, 50, 13, buttonTemplate, "Set item")
				.anchor(AnchorPoint.BOTTOM_LEFT);

		itemListArea.addElement(backButton);
		recipeArea.addElement(itemListButton);

		GuiTabbedDisplay recipeDisplay =
			(GuiTabbedDisplay) new GuiTabbedDisplay(0, 0, initialWindowWidth, initialWindowHeight)
				.addTab(recipeArea, backButton, false)
				.addTab(itemListArea, itemListButton, false)
				.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
				.setClickable(false);

		recipeTab.addElement(recipeDisplay);

		GuiScrollBar filterSelectScrollBar =
			(GuiScrollBar) new GuiScrollBar(initialWindowWidth - 18, 6, 12, 186,
				(GuiSlider) new GuiSlider(0, 21, 12, 144, 12, 15, texture, 0, 199)
					.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT))
				.addButton(new GuiButton(0,   0, 12, 11, texture, 0, 234), -10, true)
				.addButton(new GuiButton(0,  11, 12, 10, texture, 0, 214), -1, true)
				.addButton(
					(GuiButton)new GuiButton(0, 165, 12, 10, texture, 0, 224)
						.anchor(AnchorPoint.BOTTOM_RIGHT),
					1, true)
				.addButton(
					(GuiButton)new GuiButton(0, 175, 12, 11, texture, 0, 245)
						.anchor(AnchorPoint.BOTTOM_RIGHT),
					10, true)
				.anchor(AnchorPoint.TOP_RIGHT, AnchorPoint.BOTTOM_RIGHT);

		itemListArea.addElement(filterSelectScrollBar);

		FilterSelectGrid filterGrid =
			(FilterSelectGrid) new FilterSelectGrid(68, 18, initialWindowWidth - 90, 158,
				filterSelectScrollBar, texture,
				recipeCache, backButton, recipeDisplay)
					.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
					.setBackground(paneBackground);

		itemListArea.addElement(new RowCount(initialWindowWidth - 23, 6, filterGrid).anchor(AnchorPoint.TOP_RIGHT));
		itemListArea.addElement(filterGrid);

		itemListArea.addElement(
			new GuiText(68, 183, "Search", 0xff000000)
				.anchor(AnchorPoint.BOTTOM_LEFT));

		itemListArea.addElement(
				new GuiElement(106, 179, initialWindowWidth - 163, 15)
				.setBackground(paneBackground)
				.anchor(AnchorPoint.BOTTOM_LEFT, AnchorPoint.BOTTOM_RIGHT)
		);

		GuiTextInput searchInput =
			(GuiTextInput) new GuiTextInput(106, 179, 93, 15, 2, 2)
				.addListener(filterGrid)
				.anchor(AnchorPoint.BOTTOM_LEFT, AnchorPoint.BOTTOM_RIGHT);

		itemListButton.addButtonListener(searchInput);

		itemListArea.addElement(searchInput);

		class ClearButtonListener implements IButtonListener
		{
			private GuiTextInput textInput;
			public ClearButtonListener(GuiTextInput textInput)
			{
				this.textInput = textInput;
			}
			@Override
			public void onButtonEvent(GuiButton button, Event eventType)
			{
				textInput.setText("");
			}
		}

		itemListArea.addElement(
			new GuiButton(initialWindowWidth - 54, 180, 32, 13, buttonTemplate, "Clear")
				.addButtonListener(new ClearButtonListener(searchInput))
				.anchor(AnchorPoint.BOTTOM_RIGHT));

		GuiScrollBar scrollBar =
			(GuiScrollBar) new GuiScrollBar(initialWindowWidth - 18, 6, 12, 186,
				(GuiSlider) new GuiSlider(0, 21, 12, 144, 12, 15, texture, 0, 199)
					.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT))
				.addButton(new GuiButton(0,   0, 12, 11, texture, 0, 234), -10, true)
				.addButton(new GuiButton(0,  11, 12, 10, texture, 0, 214), -1, true)
				.addButton(
					(GuiButton)new GuiButton(0, 165, 12, 10, texture, 0, 224)
						.anchor(AnchorPoint.BOTTOM_RIGHT),
					1, true)
				.addButton(
					(GuiButton)new GuiButton(0, 175, 12, 11, texture, 0, 245)
						.anchor(AnchorPoint.BOTTOM_RIGHT),
					10, true)
				.anchor(AnchorPoint.TOP_RIGHT, AnchorPoint.BOTTOM_RIGHT);

		recipeArea.addElement(scrollBar);

		craftingDisplay = new CraftingDisplay(68, 18, initialWindowWidth - 90, 174, scrollBar, recipeCache);
		recipeArea.addElement(new RowCount(initialWindowWidth - 23, 6, craftingDisplay).anchor(AnchorPoint.TOP_RIGHT));
		FilterClearCallback clearCallback = new FilterClearCallback();
		clearButton.addButtonListener(clearCallback);
		clearCallback.display = craftingDisplay;

		craftingDisplay.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT);
		craftingDisplay.setBackground(paneBackground);
		recipeArea.addElement(craftingDisplay);

		return recipeTab;
	}

	private GuiElement generateTypeTab(Texture texture, ButtonTemplate buttonTemplate)
	{
		GuiElement typeTab = new GuiElement(0, 0, initialWindowWidth, initialWindowHeight)
				.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
				.setClickable(false);

		typeTab.addElement(
				new GuiElement(initialWindowWidth - 19, 5, 14, 188)
					.setBackground(paneBackground)
					.anchor(AnchorPoint.TOP_RIGHT, AnchorPoint.BOTTOM_RIGHT)
		);

		GuiScrollBar scrollBar =
			new GuiScrollBar(initialWindowWidth - 18, 6, 12, 186,
				(GuiSlider) new GuiSlider(0, 10, 12, 166, 12, 15, texture, 0, 199)
					.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT))
				.addButton(new GuiButton(0, 0, 12, 10, texture, 0, 214), -1, true)
				.addButton(
					(GuiButton) new GuiButton(0, 176, 12, 10, texture, 0, 224)
						.anchor(AnchorPoint.BOTTOM_RIGHT),
					1, true);

		scrollBar.anchor(AnchorPoint.TOP_RIGHT, AnchorPoint.BOTTOM_RIGHT);
		typeTab.addElement(scrollBar);

		typeTab.addElement(
			new CraftTypeDisplay(
				68, 6, initialWindowWidth - 90, 186, scrollBar, texture, recipeCache
			)
			.anchor(AnchorPoint.TOP_LEFT, AnchorPoint.BOTTOM_RIGHT)
			.setBackground(paneBackground)
		);

		return typeTab;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		try
		{
			uristqwerty.gui_craftguide.minecraft.Image.initJarTextures();
			((UtilImplementationCommon)Util.instance).partialTicks = f;
			guiWindow.centerOn(width / 2, height / 2);
			filter.setFilter(recipeCache.getFilter());

			super.drawScreen(mouseX, mouseY, f);
		}
		catch(Exception e)
		{
			CraftGuideLog.log(e);
		}
		catch(Error e)
		{
			CraftGuideLog.log(e);
			throw e;
		}


    	CraftGuide.side.stopTessellating();
	}

	private class FilterClearCallback implements IButtonListener
	{
		CraftingDisplay display;

		@Override
		public void onButtonEvent(GuiButton button, Event eventType)
		{
			if(eventType == Event.PRESS)
			{
				display.setFilter(null);
			}
		}
	}

	private class FilterToggle implements IButtonListener
	{
		private final SlotType type;

		public FilterToggle(SlotType type)
		{
			this.type = type;
		}

		@Override public void onButtonEvent(GuiButton button, Event eventType)
		{
			if(eventType == Event.PRESS)
			{
				GuiCraftGuide.filterSlotTypes.put(type, true);
			}
			else if(eventType == Event.RELEASE)
			{
				GuiCraftGuide.filterSlotTypes.put(type, false);
			}

			recipeCache.filter(recipeCache.getFilter());
		}
	}

	@Override
	public void handleKeyboardInput()
	{
		try
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			{
				GuiScrollBar.setScrollMultiplier(10);
			}
			else
			{
				GuiScrollBar.setScrollMultiplier(1);
			}

			super.handleKeyboardInput();
		}
		catch(Exception e)
		{
			CraftGuideLog.log(e);
		}
		catch(Error e)
		{
			CraftGuideLog.log(e);
			throw e;
		}
	}

	@Override
	public void handleMouseInput()
	{
		try
		{
			super.handleMouseInput();

		}
		catch(Exception e)
		{
			CraftGuideLog.log(e);
		}
		catch(Error e)
		{
			CraftGuideLog.log(e);
			throw e;
		}
	}

	@Override
	public int mouseWheelRate()
	{
		return CraftGuide.mouseWheelScrollRate;
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return CraftGuide.pauseWhileOpen;
	}

	public void reloadRecipes()
	{
		recipeCache.reset();
	}

	public static void onTickInGame(float f, Minecraft minecraft)
	{
		if(instance != null)
		{
			instance.drawOverlay(f, minecraft);
		}
	}

	public void drawOverlay(float f, Minecraft minecraft)
	{
		renderer.startFrame(this);
		guiOverlay.draw();
		renderer.endFrame();
	}

	@Override
	public void onGuiClosed()
	{
        Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}

	@Override
	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);

		if(CraftGuide.needsRecipeRefresh)
		{
			reloadRecipes();
		}
	}
}
