package appeng.client.gui.implementations;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.InternalSlotME;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.integration.IntegrationType;
import appeng.parts.reporting.PartTerminal;
import appeng.tile.misc.TileSecurity;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class GuiMEMonitorable extends AEBaseMEGui implements ISortSource, IConfigManagerHost
{

	GuiTabButton craftingStatusBtn;

	MEGuiTextField searchField;
	private static String memoryText = "";

	public static int CraftingGridOffsetX;
	public static int CraftingGridOffsetY;

	ItemRepo repo;

	GuiText myName;

	int offsetX = 9;
	int perRow = 9;
	int reservedSpace = 0;
	int lowerTextureOffset = 0;
	boolean customSortOrder = true;

	int rows = 0;
	int maxRows = Integer.MAX_VALUE;

	int standardSize;

	IConfigManager configSrc;

	GuiImgButton ViewBox;
	GuiImgButton SortByBox;
	GuiImgButton SortDirBox;

	GuiImgButton searchBoxSettings, terminalStyleBox;
	boolean viewCell;

	ItemStack myCurrentViewCells[] = new ItemStack[5];
	ContainerMEMonitorable monitorableContainer;

	public GuiMEMonitorable(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		this( inventoryPlayer, te, new ContainerMEMonitorable( inventoryPlayer, te ) );
	}

	public GuiMEMonitorable(InventoryPlayer inventoryPlayer, ITerminalHost te, ContainerMEMonitorable c) {

		super( c );
		myScrollBar = new GuiScrollbar();
		repo = new ItemRepo( myScrollBar, this );

		xSize = 185;
		ySize = 204;

		if ( te instanceof IViewCellStorage )
			xSize += 33;

		standardSize = xSize;

		configSrc = ((IConfigurableObject) inventorySlots).getConfigManager();
		(monitorableContainer = (ContainerMEMonitorable) inventorySlots).gui = this;

		viewCell = te instanceof IViewCellStorage;

		if ( te instanceof TileSecurity )
			myName = GuiText.Security;
		else if ( te instanceof WirelessTerminalGuiObject )
			myName = GuiText.WirelessTerminal;
		else if ( te instanceof IPortableCell )
			myName = GuiText.PortableCell;
		else if ( te instanceof IMEChest )
			myName = GuiText.Chest;
		else if ( te instanceof PartTerminal )
			myName = GuiText.Terminal;
	}

	public void postUpdate(List<IAEItemStack> list)
	{
		for (IAEItemStack is : list)
			repo.postUpdate( is );

		repo.updateView();
		setScrollBar();
	}

	private void setScrollBar()
	{
		myScrollBar.setTop( 18 ).setLeft( 175 ).setHeight( rows * 18 - 2 );
		myScrollBar.setRange( 0, (repo.size() + perRow - 1) / perRow - rows, Math.max( 1, rows / 6 ) );
	}

	public void re_init()
	{
		this.buttonList.clear();
		this.initGui();
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		memoryText = searchField.getText();
	}

	@Override
	public void initGui()
	{
		maxRows = getMaxRows();
		perRow = AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL ? 9 : 9 + ((width - standardSize) / 18);

		boolean hasNEI = AppEng.instance.isIntegrationEnabled( IntegrationType.NEI );

		int NEI = hasNEI ? 0 : 0;
		int top = hasNEI ? 22 : 0;

		int magicNumber = 114 + 1;
		int extraSpace = height - magicNumber - NEI - top - reservedSpace;

		rows = (int) Math.floor( extraSpace / 18 );
		if ( rows > maxRows )
		{
			top += (rows - maxRows) * 18 / 2;
			rows = maxRows;
		}

		if ( hasNEI )
			rows--;

		if ( rows < 3 )
			rows = 3;

		meSlots.clear();
		for (int y = 0; y < rows; y++)
		{
			for (int x = 0; x < perRow; x++)
			{
				meSlots.add( new InternalSlotME( repo, x + y * perRow, offsetX + x * 18, 18 + y * 18 ) );
			}
		}

		if ( AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) != TerminalStyle.FULL )
			this.xSize = standardSize + ((perRow - 9) * 18);
		else
			this.xSize = standardSize;

		super.initGui();
		// full size : 204
		// extra slots : 72
		// slot 18

		this.ySize = magicNumber + rows * 18 + reservedSpace;
		// this.guiTop = top;
		int unusedSpace = height - ySize;
		guiTop = (int) Math.floor( (float) unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f) );

		int offset = guiTop + 8;

		if ( customSortOrder )
		{
			buttonList.add( SortByBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_BY, configSrc.getSetting( Settings.SORT_BY ) ) );
			offset += 20;
		}

		if ( viewCell || this instanceof GuiWirelessTerm )
		{
			buttonList.add( ViewBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.VIEW_MODE, configSrc.getSetting( Settings.VIEW_MODE ) ) );
			offset += 20;
		}

		buttonList.add( SortDirBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_DIRECTION, configSrc.getSetting( Settings.SORT_DIRECTION ) ) );
		offset += 20;

		buttonList.add( searchBoxSettings = new GuiImgButton( this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance.settings
				.getSetting( Settings.SEARCH_MODE ) ) );
		offset += 20;

		if ( !(this instanceof GuiMEPortableCell) || this instanceof GuiWirelessTerm )
		{
			buttonList.add( terminalStyleBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.TERMINAL_STYLE, AEConfig.instance.settings
					.getSetting( Settings.TERMINAL_STYLE ) ) );
		}

		searchField = new MEGuiTextField( fontRendererObj, this.guiLeft + Math.max( 82, offsetX ), this.guiTop + 6, 89, fontRendererObj.FONT_HEIGHT );
		searchField.setEnableBackgroundDrawing( false );
		searchField.setMaxStringLength( 25 );
		searchField.setTextColor( 0xFFFFFF );
		searchField.setVisible( true );

		if ( viewCell || this instanceof GuiWirelessTerm )
		{
			buttonList.add( craftingStatusBtn = new GuiTabButton( this.guiLeft + 170, this.guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(),
					itemRender ) );
			craftingStatusBtn.hideEdge = 13;
		}

		// Enum setting = AEConfig.instance.getSetting( "Terminal", SearchBoxMode.class, SearchBoxMode.AUTOSEARCH );
		Enum setting = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
		searchField.setFocused( SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting );

		if ( isSubGui() )
		{
			searchField.setText( memoryText );
			repo.searchString = memoryText;
			repo.updateView();
			setScrollBar();
		}

		CraftingGridOffsetX = Integer.MAX_VALUE;
		CraftingGridOffsetY = Integer.MAX_VALUE;

		for (Object s : inventorySlots.inventorySlots)
		{
			if ( s instanceof AppEngSlot )
			{
				if ( ((AppEngSlot) s).xDisplayPosition < 197 )
					repositionSlot( (AppEngSlot) s );
			}

			if ( s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix )
			{
				Slot g = (Slot) s;
				if ( g.xDisplayPosition > 0 && g.yDisplayPosition > 0 )
				{
					CraftingGridOffsetX = Math.min( CraftingGridOffsetX, g.xDisplayPosition );
					CraftingGridOffsetY = Math.min( CraftingGridOffsetY, g.yDisplayPosition );
				}
			}
		}

		CraftingGridOffsetX -= 25;
		CraftingGridOffsetY -= 6;
	}

	protected void repositionSlot(AppEngSlot s)
	{
		s.yDisplayPosition = s.defY + ySize - 78 - 5;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		if ( btn == craftingStatusBtn )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_CRAFTING_STATUS ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		if ( btn instanceof GuiImgButton )
		{
			boolean backwards = Mouse.isButtonDown( 1 );

			GuiImgButton iBtn = (GuiImgButton) btn;
			if ( iBtn.getSetting() != Settings.ACTIONS )
			{
				Enum cv = iBtn.getCurrentValue();
				Enum next = Platform.rotateEnum( cv, backwards, iBtn.getSetting().getPossibleValues() );

				if ( btn == terminalStyleBox )
					AEConfig.instance.settings.putSetting( iBtn.getSetting(), next );
				else if ( btn == searchBoxSettings )
					AEConfig.instance.settings.putSetting( iBtn.getSetting(), next );
				else
				{
					try
					{
						NetworkHandler.instance.sendToServer( new PacketValueConfig( iBtn.getSetting().name(), next.name() ) );
					}
					catch (IOException e)
					{
						AELog.error( e );
					}
				}

				iBtn.set( next );

				if ( next.getClass() == SearchBoxMode.class || next.getClass() == TerminalStyle.class )
					re_init();
			}
		}
	}

	@Override
	protected void mouseClicked(int xCoord, int yCoord, int btn)
	{
		Enum setting = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
		if ( !(SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting) )
			searchField.mouseClicked( xCoord, yCoord, btn );

		if ( btn == 1 && searchField.isMouseIn( xCoord, yCoord ) )
		{
			searchField.setText( "" );
			repo.searchString = "";
			repo.updateView();
			setScrollBar();
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( character == ' ' && this.searchField.getText().length() == 0 )
				return;

			if ( searchField.textboxKeyTyped( character, key ) )
			{
				repo.searchString = this.searchField.getText();
				repo.updateView();
				setScrollBar();
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	@Override
	public void updateScreen()
	{
		repo.setPower( monitorableContainer.hasPower );
		super.updateScreen();
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		int x_width = 197;

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 18 );

		if ( viewCell || (this instanceof GuiSecurity) )
			this.drawTexturedModalRect( offsetX + x_width, offsetY, x_width, 0, 46, 128 );

		for (int x = 0; x < rows; x++)
			this.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18 );

		this.drawTexturedModalRect( offsetX, offsetY + 16 + rows * 18 + lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + reservedSpace - lowerTextureOffset );

		if ( viewCell )
		{
			boolean update = false;

			for (int i = 0; i < 5; i++)
			{
				if ( myCurrentViewCells[i] != monitorableContainer.cellView[i].getStack() )
				{
					update = true;
					myCurrentViewCells[i] = monitorableContainer.cellView[i].getStack();
				}
			}

			if ( update )
				repo.setViewCell( myCurrentViewCells );
		}

		if ( searchField != null )
			searchField.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/terminal.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( myName.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

	@Override
	public Enum getSortBy()
	{
		return configSrc.getSetting( Settings.SORT_BY );
	}

	@Override
	public Enum getSortDir()
	{
		return configSrc.getSetting( Settings.SORT_DIRECTION );
	}

	@Override
	public Enum getSortDisplay()
	{
		return configSrc.getSetting( Settings.VIEW_MODE );
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		if ( SortByBox != null )
			SortByBox.set( configSrc.getSetting( Settings.SORT_BY ) );

		if ( SortDirBox != null )
			SortDirBox.set( configSrc.getSetting( Settings.SORT_DIRECTION ) );

		if ( ViewBox != null )
			ViewBox.set( configSrc.getSetting( Settings.VIEW_MODE ) );

		repo.updateView();
	}

	@Override
	protected boolean isPowered()
	{
		return repo.hasPower();
	}

	int getMaxRows()
	{
		return AEConfig.instance.getConfigManager().getSetting( Settings.TERMINAL_STYLE ) == TerminalStyle.SMALL ? 6 : Integer.MAX_VALUE;
	}

}
