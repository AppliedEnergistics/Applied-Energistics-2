package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.core.localization.GuiText;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.automation.PartLevelEmitter;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiLevelEmitter extends GuiUpgradeable
{

	GuiTextField level;

	public GuiLevelEmitter(InventoryPlayer inventoryPlayer, PartLevelEmitter te) {
		super( new ContainerLevelEmitter( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();
		level = new GuiTextField( this.fontRenderer, this.guiLeft + 44, this.guiTop + 43, 59, this.fontRenderer.FONT_HEIGHT );
		level.setEnableBackgroundDrawing( false );
		level.setMaxStringLength( 16 );
		level.setTextColor( 0xFFFFFF );
		level.setVisible( true );
		level.setFocused( true );
		((ContainerLevelEmitter) inventorySlots).setTextField( level );
	}

	@Override
	protected void addButtons()
	{
		redstoneMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.REDSTONE_EMITTER, RedstoneMode.IGNORE );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( redstoneMode );
		buttonList.add( fuzzyMode );
	}

	protected void handleButtonVisiblity()
	{
		fuzzyMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
	}

	@Override
	protected void keyTyped(char character, int key)
	{
		if ( !this.checkHotbarKeys( key ) )
		{
			if ( (key == 211 || key == 205 || key == 203 || key == 14 || Character.isDigit( character )) && level.textboxKeyTyped( character, key ) )
			{
				try
				{
					String Out = level.getText();

					boolean Fixed = false;
					while (Out.startsWith( "0" ) && Out.length() > 1)
					{
						Out = Out.substring( 1 );
						Fixed = true;
					}

					if ( Fixed )
						level.setText( Out );

					if ( Out.length() == 0 )
						Out = "0";

					PacketDispatcher.sendPacketToServer( (new PacketValueConfig( "LevelEmitter.Value", Out )).getPacket() );
				}
				catch (IOException e)
				{
					e.printStackTrace();
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
		super.drawBG( offsetX, offsetY, mouseX, mouseY );
		level.drawTextBox();
	}

	protected String getBackground()
	{
		return "guis/lvlemitter.png";
	}

	protected GuiText getName()
	{
		return GuiText.LevelEmitter;
	}
}
