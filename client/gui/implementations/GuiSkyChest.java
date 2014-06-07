package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerSkyChest;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.tile.storage.TileSkyChest;

public class GuiSkyChest extends AEBaseGui
{

	public GuiSkyChest(InventoryPlayer inventoryPlayer, TileSkyChest te) {
		super( new ContainerSkyChest( inventoryPlayer, te ) );
		this.ySize = 195;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/skychest.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.SkyChest.getLocal() ), 8, 8, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 2, 4210752 );
	}

	@Override
	protected boolean enableSpaceClicking()
	{
		return !AppEng.instance.isIntegrationEnabled( "InvTweaks" );
	}

}
