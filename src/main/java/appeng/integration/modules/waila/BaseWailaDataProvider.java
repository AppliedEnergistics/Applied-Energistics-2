/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.waila;


import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;


/**
 * Base implementation for {@link mcp.mobius.waila.api.IWailaDataProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public abstract class BaseWailaDataProvider implements IWailaDataProvider
{
	@Override
	public final ItemStack getWailaStack( IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return null;
	}

	@Override
	public final List<String> getWailaHead( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return currentToolTip;
	}

	@Override
	public List<String> getWailaBody( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return currentToolTip;
	}

	@Override
	public final List<String> getWailaTail( ItemStack itemStack, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config )
	{
		return currentToolTip;
	}

	@Override
	public NBTTagCompound getNBTData( EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z )
	{
		return tag;
	}
}
