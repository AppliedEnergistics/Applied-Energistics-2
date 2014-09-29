package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;

public class GuiCraftingTerm extends GuiMEMonitorable
{

	GuiImgButton clearBtn;

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.add( clearBtn = new GuiImgButton( this.guiLeft + 92, this.guiTop + this.ySize - 156, Settings.ACTIONS, ActionItems.STASH ) );
		clearBtn.halfSize = true;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		if ( clearBtn == btn )
		{
			Slot s = null;
			Container c = inventorySlots;
			for (Object j : c.inventorySlots)
			{
				if ( j instanceof SlotCraftingMatrix )
					s = (Slot) j;
			}

			if ( s != null )
			{
				PacketInventoryAction p;
				try
				{
					p = new PacketInventoryAction( InventoryAction.MOVE_REGION, s.slotNumber, 0 );
					NetworkHandler.instance.sendToServer( p );
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}
		}
	}

	public GuiCraftingTerm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( inventoryPlayer, te, new ContainerCraftingTerm( inventoryPlayer, te ) );
		reservedSpace = 73;
	}

	@Override
	protected String getBackground()
	{
		return "guis/crafting.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		fontRendererObj.drawString( GuiText.CraftingTerminal.getLocal(), 8, ySize - 96 + 1 - reservedSpace, 4210752 );
	}

}
