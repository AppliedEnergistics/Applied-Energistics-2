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


import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class CableRenderHelper
{

	private static final CableRenderHelper INSTANCE = new CableRenderHelper();

	public static CableRenderHelper getInstance()
	{
		return INSTANCE;
	}

	public void renderStatic( final CableBusContainer cableBusContainer, final IFacadeContainer iFacadeContainer )
	{
		final TileEntity te = cableBusContainer.getTile();
		final RenderBlocksWorkaround renderer = BusRenderer.INSTANCE.getRenderer();

		if( renderer.overrideBlockTexture != null )
		{
			BusRenderHelper.INSTANCE.setPass( 0 );
		}

		if( renderer.blockAccess == null )
		{
			renderer.blockAccess = Minecraft.getMinecraft().theWorld;
		}

		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = cableBusContainer.getPart( s );
			if( part != null )
			{
				this.setSide( s );
				renderer.renderAllFaces = true;

				renderer.flipTexture = false;
				renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

				part.renderStatic( te.xCoord, te.yCoord, te.zCoord, BusRenderHelper.INSTANCE, renderer );

				renderer.setFaces( EnumSet.allOf( ForgeDirection.class ) );
				renderer.setCalculations( true );
				renderer.setUseTextures( true );
			}
		}

		if( !iFacadeContainer.isEmpty() )
		{
			/**
			 * snag list of boxes...
			 */
			final List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();

			for( final ForgeDirection s : ForgeDirection.values() )
			{
				final IPart part = cableBusContainer.getPart( s );
				if( part != null )
				{
					this.setSide( s );
					final BusRenderHelper brh = BusRenderHelper.INSTANCE;
					final BusCollisionHelper bch = new BusCollisionHelper( boxes, brh.getWorldX(), brh.getWorldY(), brh.getWorldZ(), null, true );
					part.getBoxes( bch );
				}
			}

			boolean useThinFacades = false;
			final double min = 2.0 / 16.0;
			final double max = 14.0 / 16.0;

			for( final AxisAlignedBB bb : boxes )
			{
				int o = 0;
				o += bb.maxX > max ? 1 : 0;
				o += bb.maxY > max ? 1 : 0;
				o += bb.maxZ > max ? 1 : 0;
				o += bb.minX < min ? 1 : 0;
				o += bb.minY < min ? 1 : 0;
				o += bb.minZ < min ? 1 : 0;

				if( o >= 2 )
				{
					useThinFacades = true;
				}
			}

			for( final ForgeDirection s : ForgeDirection.VALID_DIRECTIONS )
			{
				final IFacadePart fPart = iFacadeContainer.getFacade( s );

				if( fPart != null )
				{
					fPart.setThinFacades( useThinFacades );
					final AxisAlignedBB pb = fPart.getPrimaryBox();
					AxisAlignedBB b = null;
					for( final AxisAlignedBB bb : boxes )
					{
						if( bb.intersectsWith( pb ) )
						{
							if( b == null )
							{
								b = bb;
							}
							else
							{
								b.maxX = Math.max( b.maxX, bb.maxX );
								b.maxY = Math.max( b.maxY, bb.maxY );
								b.maxZ = Math.max( b.maxZ, bb.maxZ );
								b.minX = Math.min( b.minX, bb.minX );
								b.minY = Math.min( b.minY, bb.minY );
								b.minZ = Math.min( b.minZ, bb.minZ );
							}
						}
					}

					renderer.flipTexture = false;
					renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

					this.setSide( s );
					fPart.renderStatic( te.xCoord, te.yCoord, te.zCoord, BusRenderHelper.INSTANCE, renderer, iFacadeContainer, b, cableBusContainer.getPart( s ) == null );
				}
			}

			renderer.setFacade( false );
			renderer.enableAO = false;
			renderer.setTexture( null );
			renderer.setCalculations( true );
		}
	}

	private void setSide( final ForgeDirection s )
	{
		final ForgeDirection ax;
		final ForgeDirection ay;
		final ForgeDirection az;

		switch( s )
		{
			case DOWN:
				ax = ForgeDirection.EAST;
				ay = ForgeDirection.NORTH;
				az = ForgeDirection.DOWN;
				break;
			case UP:
				ax = ForgeDirection.EAST;
				ay = ForgeDirection.SOUTH;
				az = ForgeDirection.UP;
				break;
			case EAST:
				ax = ForgeDirection.SOUTH;
				ay = ForgeDirection.UP;
				az = ForgeDirection.EAST;
				break;
			case WEST:
				ax = ForgeDirection.NORTH;
				ay = ForgeDirection.UP;
				az = ForgeDirection.WEST;
				break;
			case NORTH:
				ax = ForgeDirection.WEST;
				ay = ForgeDirection.UP;
				az = ForgeDirection.NORTH;
				break;
			case SOUTH:
				ax = ForgeDirection.EAST;
				ay = ForgeDirection.UP;
				az = ForgeDirection.SOUTH;
				break;
			case UNKNOWN:
			default:
				ax = ForgeDirection.EAST;
				ay = ForgeDirection.UP;
				az = ForgeDirection.SOUTH;
				break;
		}

		BusRenderHelper.INSTANCE.setOrientation( ax, ay, az );
	}

	public void renderDynamic( final CableBusContainer cableBusContainer, final double x, final double y, final double z )
	{
		for( final ForgeDirection s : ForgeDirection.values() )
		{
			final IPart part = cableBusContainer.getPart( s );

			if( part != null )
			{
				final ForgeDirection ax;
				final ForgeDirection ay;
				final ForgeDirection az;

				switch( s )
				{
					case DOWN:
						ax = ForgeDirection.EAST;
						ay = ForgeDirection.NORTH;
						az = ForgeDirection.DOWN;
						break;
					case UP:
						ax = ForgeDirection.EAST;
						ay = ForgeDirection.SOUTH;
						az = ForgeDirection.UP;
						break;
					case EAST:
						ax = ForgeDirection.SOUTH;
						ay = ForgeDirection.UP;
						az = ForgeDirection.EAST;
						break;
					case WEST:
						ax = ForgeDirection.NORTH;
						ay = ForgeDirection.UP;
						az = ForgeDirection.WEST;
						break;
					case NORTH:
						ax = ForgeDirection.WEST;
						ay = ForgeDirection.UP;
						az = ForgeDirection.NORTH;
						break;
					case SOUTH:
						ax = ForgeDirection.EAST;
						ay = ForgeDirection.UP;
						az = ForgeDirection.SOUTH;
						break;
					case UNKNOWN:
					default:
						ax = ForgeDirection.EAST;
						ay = ForgeDirection.UP;
						az = ForgeDirection.SOUTH;
						break;
				}

				BusRenderHelper.INSTANCE.setOrientation( ax, ay, az );
				part.renderDynamic( x, y, z, BusRenderHelper.INSTANCE, BusRenderer.INSTANCE.getRenderer() );
			}
		}
	}
}
