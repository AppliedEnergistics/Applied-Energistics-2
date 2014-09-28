package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerPriority;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartStorageBus;
import appeng.tile.misc.TileInterface;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;

public class GuiPriority extends AEBaseGui
{

	GuiNumberBox priority;
	GuiTabButton originalGuiBtn;

	GuiButton plus1, plus10, plus100, plus1000;
	GuiButton minus1, minus10, minus100, minus1000;

	GuiBridge OriginalGui;

	public GuiPriority(InventoryPlayer inventoryPlayer, IPriorityHost te) {
		super( new ContainerPriority( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int a = AEConfig.instance.priorityByStacksAmounts( 0 );
		int b = AEConfig.instance.priorityByStacksAmounts( 1 );
		int c = AEConfig.instance.priorityByStacksAmounts( 2 );
		int d = AEConfig.instance.priorityByStacksAmounts( 3 );

		buttonList.add( plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 32, 22, 20, "+" + a ) );
		buttonList.add( plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 32, 28, 20, "+" + b ) );
		buttonList.add( plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 32, 32, 20, "+" + c ) );
		buttonList.add( plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 32, 38, 20, "+" + d ) );

		buttonList.add( minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 69, 22, 20, "-" + a ) );
		buttonList.add( minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 69, 28, 20, "-" + b ) );
		buttonList.add( minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 69, 32, 20, "-" + c ) );
		buttonList.add( minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 69, 38, 20, "-" + d ) );

		ItemStack myIcon = null;
		Object target = ((AEBaseContainer) inventorySlots).getTarget();

		if ( target instanceof PartStorageBus )
		{
			myIcon = AEApi.instance().parts().partStorageBus.stack( 1 );
			OriginalGui = GuiBridge.GUI_STORAGEBUS;
		}

		if ( target instanceof PartFormationPlane )
		{
			myIcon = AEApi.instance().parts().partFormationPlane.stack( 1 );
			OriginalGui = GuiBridge.GUI_FORMATION_PLANE;
		}

		if ( target instanceof TileDrive )
		{
			myIcon = AEApi.instance().blocks().blockDrive.stack( 1 );
			OriginalGui = GuiBridge.GUI_DRIVE;
		}

		if ( target instanceof TileChest )
		{
			myIcon = AEApi.instance().blocks().blockChest.stack( 1 );
			OriginalGui = GuiBridge.GUI_CHEST;
		}

		if ( target instanceof TileInterface )
		{
			myIcon = AEApi.instance().blocks().blockInterface.stack( 1 );
			OriginalGui = GuiBridge.GUI_INTERFACE;
		}

		if ( target instanceof PartInterface )
		{
			myIcon = AEApi.instance().parts().partInterface.stack( 1 );
			OriginalGui = GuiBridge.GUI_INTERFACE;
		}

		if ( OriginalGui != null )
			buttonList.add( originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );

		priority = new GuiNumberBox( fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, fontRendererObj.FONT_HEIGHT, Long.class );
		priority.setEnableBackgroundDrawing( false );
		priority.setMaxStringLength( 16 );
		priority.setTextColor( 0xFFFFFF );
		priority.setVisible( true );
		priority.setFocused( true );
		((ContainerPriority) inventorySlots).setTextField( priority );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		if ( btn == originalGuiBtn )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( OriginalGui ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		boolean isPlus = btn == plus1 || btn == plus10 || btn == plus100 || btn == plus1000;
		boolean isMinus = btn == minus1 || btn == minus10 || btn == minus100 || btn == minus1000;

		if ( isPlus || isMinus )
			addQty( getQty( btn ) );
	}

	private void addQty(int i)
	{
		try
		{
			String Out = priority.getText();

			boolean Fixed = false;
			while (Out.startsWith( "0" ) && Out.length() > 1)
			{
				Out = Out.substring( 1 );
				Fixed = true;
			}

			if ( Fixed )
				priority.setText( Out );

			if ( Out.length() == 0 )
				Out = "0";

			long result = Long.parseLong( Out );
			result += i;

			priority.setText( Out = Long.toString( result ) );

			NetworkHandler.instance.sendToServer( new PacketValueConfig( "PriorityHost.Priority", Out ) );
		}
		catch(NumberFormatException e )
		{
			// nope..
			priority.setText( "0" );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( (key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ))
					&& priority.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = priority.getText();

					boolean Fixed = false;
					while (Out.startsWith( "0" ) && Out.length() > 1)
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if ( Fixed )
						priority.setText( Out );

					if ( Out.length() == 0 )
						Out = "0";

					NetworkHandler.instance.sendToServer( new PacketValueConfig( "PriorityHost.Priority", Out ) );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
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
		bindTexture( "guis/priority.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );

		priority.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/priority.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.Priority.getLocal(), 8, 6, 4210752 );
	}
}
