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

package appeng.items.tools.quartz;


import java.util.EnumSet;

import com.google.common.base.Optional;

import crazypants.enderio.api.tool.IConduitControl;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.tools.IToolWrench;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.util.Platform;


@InterfaceList( {
    @Interface( iface = "buildcraft.api.tools.IToolWrench", iname = "BC" ),
    @Interface( iface = "crazypants.enderio.api.tool.IConduitControl", iname = "EIO") })
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench, IToolWrench, IConduitControl
{

	public ToolQuartzWrench( AEFeature type )
	{
		super( ToolQuartzWrench.class, Optional.of( type.name() ) );

		this.setFeature( EnumSet.of( type, AEFeature.QuartzWrench ) );
		this.setMaxStackSize( 1 );
		this.setHarvestLevel( "wrench", 0 );
	}

	@Override
	public boolean onItemUseFirst( ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ )
	{
		Block b = world.getBlock( x, y, z );
		if ( b != null && !player.isSneaking() && Platform.hasPermissions( new DimensionalCoord( world, x, y, z ), player ) )
		{
			if ( Platform.isClient() )
				return !world.isRemote;

			ForgeDirection mySide = ForgeDirection.getOrientation( side );
			if ( b.rotateBlock( world, x, y, z, mySide ) )
			{
				b.onNeighborBlockChange( world, x, y, z, Platform.AIR );
				player.swingItem();
				return !world.isRemote;
			}
		}
		return false;
	}

	@Override
	// public boolean shouldPassSneakingClickToBlock(World w, int x, int y, int z)
	public boolean doesSneakBypassUse( World world, int x, int y, int z, EntityPlayer player )
	{
		return true;
	}

	@Override
	public boolean canWrench( ItemStack is, EntityPlayer player, int x, int y, int z )
	{
		return true;
	}

	@Override
	public boolean canWrench( EntityPlayer player, int x, int y, int z )
	{
		return true;
	}

	@Override
	public void wrenchUsed( EntityPlayer player, int x, int y, int z )
	{
		player.swingItem();
    }

    @Override
    public boolean shouldHideFacades(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean showOverlay(ItemStack stack, EntityPlayer player)
    {
        return true;
    }
}
