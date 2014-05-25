package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;

public class GuiCraftAmount extends AEBaseGui
{

	GuiTextField amountToCraft;
	GuiTabButton originalGuiBtn;

	GuiButton next;

	GuiButton plus1, plus10, plus100, plus1000;
	GuiButton minus1, minus10, minus100, minus1000;

	GuiBridge OriginalGui;

	public GuiCraftAmount(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftAmount( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		buttonList.add( next = new GuiButton( 0, this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.getLocal() ) );

		buttonList.add( plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 26, 22, 20, "+1" ) );
		buttonList.add( plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 26, 28, 20, "+10" ) );
		buttonList.add( plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 26, 32, 20, "+100" ) );
		buttonList.add( plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 26, 38, 20, "+1000" ) );

		buttonList.add( minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 75, 22, 20, "-1" ) );
		buttonList.add( minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 75, 28, 20, "-10" ) );
		buttonList.add( minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 75, 32, 20, "-100" ) );
		buttonList.add( minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 75, 38, 20, "-1000" ) );

		ItemStack myIcon = null;
		Object target = ((AEBaseContainer) inventorySlots).getTarget();

		if ( target instanceof WirelessTerminalGuiObject )
		{
			myIcon = AEApi.instance().items().itemWirelessTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if ( target instanceof PartTerminal )
		{
			myIcon = AEApi.instance().parts().partTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_ME;
		}

		if ( target instanceof PartCraftingTerminal )
		{
			myIcon = AEApi.instance().parts().partCraftingTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if ( target instanceof PartPatternTerminal )
		{
			myIcon = AEApi.instance().parts().partPatternTerminal.stack( 1 );
			OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}

		if ( OriginalGui != null )
			buttonList.add( originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );

		amountToCraft = new GuiTextField( fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, fontRendererObj.FONT_HEIGHT );
		amountToCraft.setEnableBackgroundDrawing( false );
		amountToCraft.setMaxStringLength( 16 );
		amountToCraft.setTextColor( 0xFFFFFF );
		amountToCraft.setVisible( true );
		amountToCraft.setFocused( true );
		amountToCraft.setText( "1" );
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

		if ( btn == plus1 )
			addQty( 1 );
		if ( btn == plus10 )
			addQty( 10 );
		if ( btn == plus100 )
			addQty( 100 );
		if ( btn == plus1000 )
			addQty( 1000 );
		if ( btn == minus1 )
			addQty( -1 );
		if ( btn == minus10 )
			addQty( -10 );
		if ( btn == minus100 )
			addQty( -100 );
		if ( btn == minus1000 )
			addQty( -1000 );
	}

	private void addQty(int i)
	{
		try
		{
			String Out = amountToCraft.getText();

			boolean Fixed = false;
			while (Out.startsWith( "0" ) && Out.length() > 1)
			{
				Out = Out.substring( 1 );
				Fixed = true;
			}

			if ( Fixed )
				amountToCraft.setText( Out );

			if ( Out.length() == 0 )
				Out = "0";

			long result = Long.parseLong( Out );

			if ( result == 1 && i > 1 )
				result = 0;

			result += i;
			if ( result < 1 )
				result = 1;

			amountToCraft.setText( Out = Long.toString( result ) );
		}
		catch (NumberFormatException e)
		{
			// :P
		}
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( (key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ))
					&& amountToCraft.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = amountToCraft.getText();

					boolean Fixed = false;
					while (Out.startsWith( "0" ) && Out.length() > 1)
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if ( Fixed )
						amountToCraft.setText( Out );

					if ( Out.length() == 0 )
						Out = "0";

					long result = Long.parseLong( Out );
					if ( result < 0 )
					{
						amountToCraft.setText( "1" );
					}
				}
				catch (NumberFormatException e)
				{
					// :P
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
		bindTexture( "guis/craftAmt.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );

		try
		{
			Long.parseLong( amountToCraft.getText() );
			next.enabled = amountToCraft.getText().length() > 0;
		}
		catch (NumberFormatException e)
		{
			next.enabled = false;
		}

		amountToCraft.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/craftAmt.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.SelectAmount.getLocal(), 8, 6, 4210752 );
	}
}
