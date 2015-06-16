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

package appeng.block.storage;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderDrive;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;


public class BlockDrive extends AEBaseTileBlock
{

	public BlockDrive()
	{
		super( Material.iron );
		this.setTileEntity( TileDrive.class );
		this.setFeature( EnumSet.of( AEFeature.StorageCells, AEFeature.MEDrive ) );
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderDrive.class;
	}
	
	@Override
	public boolean onActivated(
			World w,
			BlockPos pos,
			EntityPlayer p,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		TileDrive tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_DRIVE );
			}
			return true;
		}
		return false;
	}
}
