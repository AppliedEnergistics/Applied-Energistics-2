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

package appeng.client.render;


import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import appeng.block.AEBaseBlock;
import appeng.core.AELog;
import appeng.tile.AEBaseTile;


@SideOnly( Side.CLIENT )
public class TESRWrapper extends TileEntitySpecialRenderer
{

	private final ModelGenerator renderBlocksInstance = new ModelGenerator();
	private final BaseBlockRender blkRender;
	private final double maxDistance;

	public TESRWrapper( final BaseBlockRender render )
	{
		this.blkRender = render;
		this.maxDistance = this.blkRender.getTesrRenderDistance();
	}

	@Override
	public final void renderTileEntityAt( final TileEntity te, final double x, final double y, final double z, final float f, final int something )
	{
		if( te instanceof AEBaseTile )
		{
			final Block b = te.getBlockType();

			if( b instanceof AEBaseBlock && ( (AEBaseTile) te ).requiresTESR() )
			{
				if( Math.abs( x ) > this.maxDistance || Math.abs( y ) > this.maxDistance || Math.abs( z ) > this.maxDistance )
				{
					return;
				}

				final Tessellator tess = Tessellator.getInstance();

				try
				{
					GL11.glPushMatrix();

					this.renderBlocksInstance.setBlockAccess( te.getWorld() );
					this.blkRender.renderTile( (AEBaseBlock) b, (AEBaseTile) te, tess.getWorldRenderer(), x, y, z, f, this.renderBlocksInstance );

					GL11.glPopMatrix();
				}
				catch( final Throwable t )
				{
					AELog.severe( "Hi, Looks like there was a crash while rendering something..." );
					t.printStackTrace();
					AELog.severe( "MC will now crash ( probably )!" );
					throw new IllegalStateException( t );
				}
			}
		}
	}
}
