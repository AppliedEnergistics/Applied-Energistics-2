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

package appeng.block.grindstone;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.grindstone.TileGrinder;
import appeng.util.Platform;


public class BlockGrinder extends AEBaseBlock
{

	public BlockGrinder()
	{
		super( BlockGrinder.class, Material.rock );
		this.setFeature( EnumSet.of( AEFeature.GrindStone ) );
		this.setTileEntity( TileGrinder.class );
		this.setHardness( 3.2F );
	}

	@Override
	public boolean onActivated( World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ )
	{
		TileGrinder tg = this.getTileEntity( w, x, y, z );
		if( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_GRINDER );
			return true;
		}
		return false;
	}
}
