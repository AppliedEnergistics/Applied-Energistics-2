package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerTeleporter;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.tile.misc.TileTeleporter;

public class GuiTeleporter extends AEBaseGui 
{

	GuiNumberBox frequency;
	
	public GuiTeleporter(InventoryPlayer inventoryPlayer, TileTeleporter te) {
		super(new ContainerTeleporter(inventoryPlayer, te));
	}

	@Override
	public void initGui() {
		super.initGui();
		frequency = new GuiNumberBox(fontRendererObj, this.guiLeft + 62,
				this.guiTop + 57, 59, fontRendererObj.FONT_HEIGHT, Long.class);
		frequency.setEnableBackgroundDrawing(false);
		frequency.setMaxStringLength(16);
		frequency.setTextColor(0xFFFFFF);
		frequency.setVisible(true);
		frequency.setFocused(true);
		((ContainerTeleporter) inventorySlots).setTextField(frequency);
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( (key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ))
					&& frequency.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = frequency.getText();

					boolean Fixed = false;
					while (Out.startsWith( "0" ) && Out.length() > 1)
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if ( Fixed )
						frequency.setText( Out );

					if ( Out.length() == 0 )
						Out = "0";

					NetworkHandler.instance.sendToServer( new PacketValueConfig( "FrequencyHost.Frequency", Out ) );
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

		frequency.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/priority.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.Teleporter.getLocal(), 8, 6, 4210752 );
	}
}
