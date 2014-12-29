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

package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RendererSecurity;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileSecurity;
import appeng.util.Platform;

public class BlockSecurity extends AEBaseBlock
{

	public BlockSecurity() {
		super( BlockSecurity.class, Material.iron );
		this.setFeature( EnumSet.of( AEFeature.Security ) );
		this.setTileEntity( TileSecurity.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RendererSecurity.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileSecurity tg = this.getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_SECURITY );
			return true;

		}
		return false;
	}

}
