package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.localization.GuiText;

public class GuiPatternTerm extends GuiMEMonitorable
{

	GuiImgButton exttractPatternBtn;
	GuiImgButton substitutionsBtn;

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.add( exttractPatternBtn = new GuiImgButton( this.guiLeft + 6, this.guiTop + this.ySize - 161, Settings.ACTIONS, ActionItems.PULL ) );
		buttonList.add( substitutionsBtn = new GuiImgButton( this.guiLeft + 118, this.guiTop + this.ySize - 161, Settings.ACTIONS, ActionItems.CLOSE ) );
		exttractPatternBtn.halfSize = true;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		if ( exttractPatternBtn == btn )
		{

		}

		if ( substitutionsBtn == btn )
		{

		}
	}

	public GuiPatternTerm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( inventoryPlayer, te, new ContainerPatternTerm( inventoryPlayer, te ) );
		reservedSpace = 85;
	}

	protected String getBackground()
	{
		return "guis/pattern.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		fontRendererObj.drawString( GuiText.PatternTerminal.getLocal(), 8, ySize - 96 + 2 - reservedSpace, 4210752 );
	}

}
