package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.localization.GuiText;

public class GuiCraftConfirm extends AEBaseGui
{

	public GuiCraftConfirm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftConfirm( inventoryPlayer, te ) );
	}

	@Override
	public void initGui()
	{
		super.initGui();
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/craftingreport.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	protected String getBackground()
	{
		return "guis/craftingreport.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.ConfirmCrafting.getLocal(), 8, 6, 4210752 );
	}
}
