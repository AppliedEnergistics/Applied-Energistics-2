/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.modules.NEIHelpers;


import appeng.client.gui.implementations.GuiMEMonitorable;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

import java.util.ArrayList;


public class TerminalCraftingSlotFinder implements IStackPositioner
{

	@Override
	public ArrayList<PositionedStack> positionStacks( final ArrayList<PositionedStack> a )
	{
		for( final PositionedStack ps : a )
		{
			if( ps != null )
			{
				ps.relx += GuiMEMonitorable.craftingGridOffsetX;
				ps.rely += GuiMEMonitorable.craftingGridOffsetY;
			}
		}
		return a;
	}
}
