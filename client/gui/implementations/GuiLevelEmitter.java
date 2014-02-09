package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.automation.PartLevelEmitter;

public class GuiLevelEmitter extends GuiUpgradeable
{

	GuiTextField level;

	GuiButton plus1, plus10, plus100, plus1000;
	GuiButton minus1, minus10, minus100, minus1000;

	public GuiLevelEmitter(InventoryPlayer inventoryPlayer, PartLevelEmitter te) {
		super( new ContainerLevelEmitter( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		level = new GuiTextField( fontRendererObj, this.guiLeft + 44, this.guiTop + 43, 59, fontRendererObj.FONT_HEIGHT );
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
		redstoneMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 17, 22, 20, "+1" ) );
		buttonList.add( plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 17, 28, 20, "+10" ) );
		buttonList.add( plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 17, 32, 20, "+100" ) );
		buttonList.add( plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 17, 38, 20, "+1000" ) );

		buttonList.add( minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 59, 22, 20, "-1" ) );
		buttonList.add( minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 59, 28, 20, "-10" ) );
		buttonList.add( minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 59, 32, 20, "-100" ) );
		buttonList.add( minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 59, 38, 20, "-1000" ) );

		buttonList.add( redstoneMode );
		buttonList.add( fuzzyMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

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

			long result = Long.parseLong( Out );
			result += i;
			if ( result < 0 )
				result = 0;

			level.setText( Out = Long.toString( result ) );

			NetworkHandler.instance.sendToServer( new PacketValueConfig( "LevelEmitter.Value", Out ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
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

					NetworkHandler.instance.sendToServer( new PacketValueConfig( "LevelEmitter.Value", Out ) );
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
