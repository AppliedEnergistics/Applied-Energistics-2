package appeng.client.gui.implementations;

import java.io.IOException;

import appeng.api.config.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.automation.PartLevelEmitter;

public class GuiLevelEmitter extends GuiUpgradeable
{

	GuiNumberBox level;

	GuiButton plus1, plus10, plus100, plus1000;
	GuiButton minus1, minus10, minus100, minus1000;

	GuiImgButton levelMode;
	GuiImgButton craftingMode;

	public GuiLevelEmitter(InventoryPlayer inventoryPlayer, PartLevelEmitter te) {
		super( new ContainerLevelEmitter( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		level = new GuiNumberBox( fontRendererObj, this.guiLeft + 24, this.guiTop + 43, 79, fontRendererObj.FONT_HEIGHT, Long.class );
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
		levelMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL );
		redstoneMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		modMode = new GuiImgButton( this.guiLeft - 18, guiTop + 68, Settings.MOD_MODE, ModMode.FILTER_BY_ITEM );
		craftingMode = new GuiImgButton( this.guiLeft - 18, guiTop + 88, Settings.CRAFT_VIA_REDSTONE, YesNo.NO );

		int a = AEConfig.instance.levelByStackAmounts( 0 );
		int b = AEConfig.instance.levelByStackAmounts( 1 );
		int c = AEConfig.instance.levelByStackAmounts( 2 );
		int d = AEConfig.instance.levelByStackAmounts( 3 );

		buttonList.add( plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a ) );
		buttonList.add( plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b ) );
		buttonList.add( plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c ) );
		buttonList.add( plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d ) );

		buttonList.add( minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a ) );
		buttonList.add( minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b ) );
		buttonList.add( minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c ) );
		buttonList.add( minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d ) );

		buttonList.add( levelMode );
		buttonList.add( redstoneMode );
		buttonList.add( fuzzyMode );
		buttonList.add( modMode );
		buttonList.add( craftingMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		try
		{
			if ( btn == craftingMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( craftingMode.getSetting(), backwards ) );

			if ( btn == levelMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( levelMode.getSetting(), backwards ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}

		boolean isPlus = btn == plus1 || btn == plus10 || btn == plus100 || btn == plus1000;
		boolean isMinus = btn == minus1 || btn == minus10 || btn == minus100 || btn == minus1000;

		if ( isPlus || isMinus )
			addQty( getQty( btn ) );
	}

	private void addQty(long i)
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
		catch (NumberFormatException e)
		{
			// nope..
			level.setText( "0" );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	protected void handleButtonVisibility()
	{
		craftingMode.setVisibility( bc.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 );
		fuzzyMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
		modMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
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
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		boolean notCraftingMode = bc.getInstalledUpgrades( Upgrades.CRAFTING ) == 0;

		// configure enabled status...
		level.setEnabled( notCraftingMode );
		plus1.enabled = notCraftingMode;
		plus10.enabled = notCraftingMode;
		plus100.enabled = notCraftingMode;
		plus1000.enabled = notCraftingMode;
		minus1.enabled = notCraftingMode;
		minus10.enabled = notCraftingMode;
		minus100.enabled = notCraftingMode;
		minus1000.enabled = notCraftingMode;
		levelMode.enabled = notCraftingMode;
		redstoneMode.enabled = notCraftingMode;

		super.drawFG( offsetX, offsetY, mouseX, mouseY );

		if ( craftingMode != null )
			craftingMode.set( ((ContainerLevelEmitter) cvb).cmType );

		if ( levelMode != null )
			levelMode.set( ((ContainerLevelEmitter) cvb).lvType );
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
