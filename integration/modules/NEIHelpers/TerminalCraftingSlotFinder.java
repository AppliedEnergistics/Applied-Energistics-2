package appeng.integration.modules.NEIHelpers;

import java.util.ArrayList;

import appeng.client.gui.implementations.GuiMEMonitorable;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

public class TerminalCraftingSlotFinder implements IStackPositioner
{

	@Override
	public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> a)
	{
		for (PositionedStack ps : a)
			if ( ps != null )
			{
				ps.relx += GuiMEMonitorable.CraftingGridOffsetX;
				ps.rely += GuiMEMonitorable.CraftingGridOffsetY;
			}
		return a;
	}

}
