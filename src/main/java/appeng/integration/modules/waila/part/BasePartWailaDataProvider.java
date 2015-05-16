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

package appeng.integration.modules.waila.part;


import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import appeng.api.parts.IPart;


/**
 * Default implementation of {@link appeng.integration.modules.waila.part.IPartWailaDataProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public abstract class BasePartWailaDataProvider implements IPartWailaDataProvider
{
	@Override
	public ItemStack getWailaStack( IPart part, IWailaConfigHandler config, ItemStack partStack )
	{
		return null;
	}

	@Override
	public List<String> getWailaHead( IPart part, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return currentToolTip;
	}

	@Override
	public List<String> getWailaBody( IPart part, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return currentToolTip;
	}

	@Override
	public List<String> getWailaTail( IPart part, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return currentToolTip;
	}

	@Override
	public NBTTagCompound getNBTData( EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z )
	{
		return tag;
	}
}
