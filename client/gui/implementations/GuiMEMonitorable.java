package appeng.client.gui.implementations;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.InternalSlotME;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartTerminal;
import appeng.tile.misc.TileSecurity;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class GuiMEMonitorable extends AEBaseMEGui implements ISortSource, IConfigManagerHost
{

	MEGuiTextField searchField;
	ItemRepo repo;

	GuiText myName;

	int xoffset = 9;
	int perRow = 9;
	int reservedSpace = 0;
	int lowerTextureOffset = 0;
	boolean customSortOrder = true;

	int rows = 0;
	int maxRows = Integer.MAX_VALUE;

	IConfigManager configSrc;

	GuiImgButton ViewBox;
	GuiImgButton SortByBox;
	GuiImgButton SortDirBox;

	GuiImgButton searchBoxSettings;

	public GuiMEMonitorable(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		this( inventoryPlayer, te, new ContainerMEMonitorable( inventoryPlayer, null ) );
	}

	public GuiMEMonitorable(InventoryPlayer inventoryPlayer, ITerminalHost te, ContainerMEMonitorable c) {

		super( c );
		myScrollBar = new GuiScrollbar();
		repo = new ItemRepo( myScrollBar, this );
		xSize = 195;
		ySize = 204;

		configSrc = ((IConfigureableObject) inventorySlots).getConfigManager();
		((ContainerMEMonitorable) inventorySlots).gui = this;

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
	public void initGui()
	{
		int NEI = 0;
		int top = 4;
		int magicNumber = 114 + 1;
		int extraSpace = height - magicNumber - NEI - top - reservedSpace;

		rows = (int) Math.floor( extraSpace / 18 );
		if ( rows > maxRows )
		{
			top += (rows - maxRows) * 18 / 2;
			rows = maxRows;
		}

		meSlots.clear();
		for (int y = 0; y < rows; y++)
		{
			for (int x = 0; x < perRow; x++)
			{
				meSlots.add( new InternalSlotME( repo, x + y * perRow, xoffset + x * 18, 18 + y * 18 ) );
			}
		}

		super.initGui();
		// full size : 204
		// extra slots : 72
		// slot 18

		this.ySize = magicNumber + rows * 18 + reservedSpace;
		this.guiTop = top;

		int offset = guiTop + 8;

		if ( customSortOrder )
		{
			buttonList.add( SortByBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_BY, configSrc.getSetting( Settings.SORT_BY ) ) );
			offset += 20;
		}

		buttonList.add( SortDirBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.SORT_DIRECTION, configSrc.getSetting( Settings.SORT_DIRECTION ) ) );
		offset += 20;

		buttonList.add( searchBoxSettings = new GuiImgButton( this.guiLeft - 18, offset, Settings.SEARCH_MODE, AEConfig.instance.settings
				.getSetting( Settings.SEARCH_MODE ) ) );

		searchField = new MEGuiTextField( fontRendererObj, this.guiLeft + Math.max( 82, xoffset ), this.guiTop + 6, 89, fontRendererObj.FONT_HEIGHT );
		searchField.setEnableBackgroundDrawing( false );
		searchField.setMaxStringLength( 25 );
		searchField.setTextColor( 0xFFFFFF );
		searchField.setVisible( true );

		// Enum setting = AEConfig.instance.getSetting( "Terminal", SearchBoxMode.class, SearchBoxMode.AUTOSEARCH );
		Enum setting = AEConfig.instance.settings.getSetting( Settings.SEARCH_MODE );
		searchField.setFocused( SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting );

		setScrollBar();

		for (Object s : inventorySlots.inventorySlots)
		{
			if ( s instanceof AppEngSlot )
			{
				if ( ((AppEngSlot) s).xDisplayPosition < 197 )
					((AppEngSlot) s).yDisplayPosition = ((AppEngSlot) s).defY + ySize - 78 - 5;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		if ( btn instanceof GuiImgButton )
		{
			GuiImgButton iBtn = (GuiImgButton) btn;

			Enum cv = iBtn.getCurrentValue();
			Enum next = Platform.nextEnum( cv );

			if ( btn == searchBoxSettings )
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

			if ( next.getClass() == SearchBoxMode.class )
				re_init();
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
		}

		super.mouseClicked( xCoord, yCoord, btn );
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
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
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		int x_width = 197;

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, x_width, 18 );
		this.drawTexturedModalRect( offsetX + x_width, offsetY, x_width, 0, 46, 128 );

		for (int x = 0; x < rows; x++)
			this.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18 );

		this.drawTexturedModalRect( offsetX, offsetY + 16 + rows * 18 + lowerTextureOffset, 0, 106 - 18 - 18, x_width, 99 + reservedSpace - lowerTextureOffset );

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
		fontRendererObj.drawString( myName.getLocal(), 8, 6, 4210752 );
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

}
