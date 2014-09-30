/**
 * 
 */
package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;

public class GuiCraftingStatus extends GuiCraftingCPU
{

	final ContainerCraftingStatus ccc;
	GuiButton selectCPU;

	GuiTabButton originalGuiBtn;
	GuiBridge OriginalGui;
	ItemStack myIcon = null;

	public GuiCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftingStatus( inventoryPlayer, te ) );

		ccc = (ContainerCraftingStatus) inventorySlots;
		Object target = ccc.getTarget();

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
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == selectCPU )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

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
	}

	@Override
	protected String getGuiDisplayName(String in)
	{
		return in; // the cup name is on the button
	}

	@Override
	public void initGui()
	{
		super.initGui();

		selectCPU = new GuiButton( 0, this.guiLeft + 8, this.guiTop + ySize - 25, 150, 20, GuiText.CraftingCPU.getLocal() + ": " + GuiText.NoCraftingCPUs );
		// selectCPU.enabled = false;
		buttonList.add( selectCPU );

		if ( myIcon != null )
		{
			buttonList.add( originalGuiBtn = new GuiTabButton( this.guiLeft + 213, this.guiTop - 4, myIcon, myIcon.getDisplayName(), itemRender ) );
			originalGuiBtn.hideEdge = 13;
		}
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.NoCraftingJobs.getLocal();

		if ( ccc.selectedCpu >= 0 )// && ccc.selectedCpu < ccc.cpus.size() )
		{
			if ( ccc.myName.length() > 0 )
			{
				String name = ccc.myName.substring( 0, Math.min( 20, ccc.myName.length() ) );
				btnTextText = GuiText.CPUs.getLocal() + ": " + name;
			}
			else
				btnTextText = GuiText.CPUs.getLocal() + ": #" + ccc.selectedCpu;
		}

		if ( ccc.noCPU )
			btnTextText = GuiText.NoCraftingJobs.getLocal();

		selectCPU.displayString = btnTextText;
	}

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		updateCPUButtonText();
		super.drawScreen( mouse_x, mouse_y, btn );
	}
}
