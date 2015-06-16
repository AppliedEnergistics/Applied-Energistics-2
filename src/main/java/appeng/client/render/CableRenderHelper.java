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


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.util.AEAxisAlignedBB;
import appeng.api.util.AEPartLocation;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusContainer;


public class CableRenderHelper
{

	private static final CableRenderHelper INSTANCE = new CableRenderHelper();

	public static CableRenderHelper getInstance()
	{
		return INSTANCE;
	}

	public void renderStatic( CableBusContainer cableBusContainer, IFacadeContainer iFacadeContainer )
	{
		TileEntity te = cableBusContainer.getTile();
		IRenderHelper renderer = BusRenderer.INSTANCE.renderer;

		if( renderer.overrideBlockTexture != null )
		{
			BusRenderHelper.INSTANCE.setPass( 0 );
		}
		else
		{
			BusRenderHelper.INSTANCE.setPass( MinecraftForgeClient.getRenderLayer() == EnumWorldBlockLayer.TRANSLUCENT ? 1 : 0 );
		}

		if( renderer.blockAccess == null )
		{
			renderer.blockAccess = Minecraft.getMinecraft().theWorld;
		}

		for( AEPartLocation s : AEPartLocation.values() )
		{
			IPart part = cableBusContainer.getPart( s );
			if( part != null )
			{
				this.setSide( s );
				renderer.renderAllFaces = true;

				//renderer.flipTexture = false;
				renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
				renderer.setOverrideBlockTexture( null );

				part.renderStatic( te.getPos(), BusRenderHelper.INSTANCE, renderer );

				//renderer.faces = EnumSet.allOf( EnumFacing.class );
				//renderer.useTextures = true;
			}
		}

		if( !iFacadeContainer.isEmpty() )
		{
			/**
			 * snag list of boxes...
			 */
			List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
			for( AEPartLocation s : AEPartLocation.values() )
			{
				IPart part = cableBusContainer.getPart( s );
				if( part != null )
				{
					this.setSide( s );
					BusRenderHelper brh = BusRenderHelper.INSTANCE;
					BusCollisionHelper bch = new BusCollisionHelper( boxes, brh.getWorldX(), brh.getWorldY(), brh.getWorldZ(), null, true );
					part.getBoxes( bch );
				}
			}

			boolean useThinFacades = false;
			double min = 2.0 / 16.0;
			double max = 14.0 / 16.0;

			for( AxisAlignedBB bb : boxes )
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

			for( AEPartLocation s : AEPartLocation.SIDE_LOCATIONS )
			{
				IFacadePart fPart = iFacadeContainer.getFacade( s );
				if( fPart != null )
				{
					AEAxisAlignedBB b = null;
					fPart.setThinFacades( useThinFacades );
					AxisAlignedBB pb = fPart.getPrimaryBox();
					for( AxisAlignedBB bb : boxes )
					{
						if( bb.intersectsWith( pb ) )
						{
							if( b == null )
							{
								b = AEAxisAlignedBB.fromBounds( bb );
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
					renderer.setOverrideBlockTexture( null );

					this.setSide( s );
					
					fPart.renderStatic( te.getPos(), BusRenderHelper.INSTANCE, renderer, iFacadeContainer, b == null ? null : b.getBoundingBox(), cableBusContainer.getPart( s ) == null );
				}
			}

			//renderer.isFacade = false;
			//renderer.enableAO = false;
			// renderer.sett
		}
	}

	private void setSide( AEPartLocation s )
	{
		EnumFacing ax;
		EnumFacing ay;
		EnumFacing az;

		switch( s )
		{
			case DOWN:
				ax = EnumFacing.EAST;
				ay = EnumFacing.NORTH;
				az = EnumFacing.DOWN;
				break;
			case UP:
				ax = EnumFacing.EAST;
				ay = EnumFacing.SOUTH;
				az = EnumFacing.UP;
				break;
			case EAST:
				ax = EnumFacing.SOUTH;
				ay = EnumFacing.UP;
				az = EnumFacing.EAST;
				break;
			case WEST:
				ax = EnumFacing.NORTH;
				ay = EnumFacing.UP;
				az = EnumFacing.WEST;
				break;
			case NORTH:
				ax = EnumFacing.WEST;
				ay = EnumFacing.UP;
				az = EnumFacing.NORTH;
				break;
			case SOUTH:
				ax = EnumFacing.EAST;
				ay = EnumFacing.UP;
				az = EnumFacing.SOUTH;
				break;
			default:
				ax = EnumFacing.EAST;
				ay = EnumFacing.UP;
				az = EnumFacing.SOUTH;
				break;
		}

		BusRenderHelper.INSTANCE.setOrientation( ax, ay, az );
	}

	public void renderDynamic( CableBusContainer cableBusContainer, double x, double y, double z )
	{
		for( EnumFacing s : EnumFacing.values() )
		{
			IPart part = cableBusContainer.getPart( s );
			if( part != null )
			{
				EnumFacing ax;
				EnumFacing ay;
				EnumFacing az;

				switch( s )
				{
					case DOWN:
						ax = EnumFacing.EAST;
						ay = EnumFacing.NORTH;
						az = EnumFacing.DOWN;
						break;
					case UP:
						ax = EnumFacing.EAST;
						ay = EnumFacing.SOUTH;
						az = EnumFacing.UP;
						break;
					case EAST:
						ax = EnumFacing.SOUTH;
						ay = EnumFacing.UP;
						az = EnumFacing.EAST;
						break;
					case WEST:
						ax = EnumFacing.NORTH;
						ay = EnumFacing.UP;
						az = EnumFacing.WEST;
						break;
					case NORTH:
						ax = EnumFacing.WEST;
						ay = EnumFacing.UP;
						az = EnumFacing.NORTH;
						break;
					case SOUTH:
						ax = EnumFacing.EAST;
						ay = EnumFacing.UP;
						az = EnumFacing.SOUTH;
						break;
					default:
						ax = EnumFacing.EAST;
						ay = EnumFacing.UP;
						az = EnumFacing.SOUTH;
						break;
				}

				BusRenderHelper.INSTANCE.setOrientation( ax, ay, az );
				part.renderDynamic( x, y, z, BusRenderHelper.INSTANCE, BusRenderer.INSTANCE.renderer );
			}
		}
	}
}
