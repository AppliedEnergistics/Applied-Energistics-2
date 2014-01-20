package appeng.client.gui.implementations;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.Settings;
import appeng.api.implementations.IMEChest;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.me.InternalSlotME;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.AppEngSlot;
import appeng.core.Configuration;
import appeng.core.localization.GuiText;
import appeng.helpers.ICellItemViewer;
import appeng.parts.reporting.PartTerminal;
import appeng.util.Platform;

public class GuiMEMonitorable extends AEBaseMEGui
{

	GuiTextField searchField;
	ItemRepo repo;

	GuiText myName;
	int rows = 0;
	int maxRows = Integer.MAX_VALUE;

	public GuiMEMonitorable(InventoryPlayer inventoryPlayer, IStorageMonitorable te) {
		super( new ContainerMEMonitorable( inventoryPlayer, null ) );
		myScrollBar = new GuiScrollbar();
		repo = new ItemRepo( myScrollBar );
		xSize = 195;
		ySize = 204;

		if ( te instanceof ICellItemViewer )
			myName = GuiText.PortableCell;
		if ( te instanceof IMEChest )
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
		myScrollBar.setRange( 0, (repo.size() + 8) / 9 - rows, Math.max( 1, rows / 6 ) );
	}

	@Override
	public void initGui()
	{
		int NEI = 0;
		int top = 4;
		int extraSpace = height - 114 - NEI - top;
		int moveDown = 0;

		rows = (int) Math.floor( extraSpace / 18 );
		if ( rows > maxRows )
		{
			top += (rows - maxRows) * 18 / 2;
			rows = maxRows;
		}

		meSlots.clear();
		for (int y = 0; y < rows; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				meSlots.add( new InternalSlotME( repo, x + y * 9, 9 + x * 18, 18 + y * 18 ) );
			}
		}

		super.initGui();
		// full size : 204
		// extra slots : 72
		// slot 18

		this.ySize = 114 + rows * 18;
		this.guiTop = top;

		buttonList.add( new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.SORT_BY, Configuration.instance.settings.getSetting( Settings.SORT_BY ) ) );
		buttonList.add( new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.SORT_DIRECTION, Configuration.instance.settings
				.getSetting( Settings.SORT_DIRECTION ) ) );

		searchField = new GuiTextField( this.fontRenderer, this.guiLeft + 82, this.guiTop + 6, 89, this.fontRenderer.FONT_HEIGHT );
		searchField.setEnableBackgroundDrawing( false );
		searchField.setMaxStringLength( 25 );
		searchField.setTextColor( 0xFFFFFF );
		searchField.setVisible( true );
		searchField.setFocused( true );

		setScrollBar();

		for (Object s : inventorySlots.inventorySlots)
		{
			if ( s instanceof AppEngSlot )
			{
				((AppEngSlot) s).yDisplayPosition = ((AppEngSlot) s).defY + ySize - 78 - 4;
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
			Configuration.instance.settings.putSetting( iBtn.getSetting(), next );
			iBtn.set( next );
			repo.updateView();
		}
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
		bindTexture( "guis/terminal.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, 18 );

		for (int x = 0; x < rows; x++)
			this.drawTexturedModalRect( offsetX, offsetY + 18 + x * 18, 0, 18, xSize, 18 );

		this.drawTexturedModalRect( offsetX, offsetY + 16 + rows * 18, 0, 106, xSize, 98 );

		searchField.drawTextBox();
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( myName.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
