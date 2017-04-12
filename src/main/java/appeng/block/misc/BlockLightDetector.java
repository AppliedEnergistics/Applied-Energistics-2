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


import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderQuartzTorch;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.helpers.MetaRotation;
import appeng.tile.misc.TileLightDetector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;


public class BlockLightDetector extends AEBaseTileBlock implements IOrientableBlock, ICustomCollision
{

	public BlockLightDetector()
	{
		super( Material.circuits );

		this.setLightOpacity( 0 );
		this.isFullSize = false;
		this.isOpaque = false;

		this.setTileEntity( TileLightDetector.class );
		this.setFeature( EnumSet.of( AEFeature.LightDetector ) );
	}

	@Override
	public int isProvidingWeakPower( final IBlockAccess w, final int x, final int y, final int z, final int side )
	{
		if( w instanceof World && ( (TileLightDetector) this.getTileEntity( w, x, y, z ) ).isReady() )
		{
			return ( (World) w ).getBlockLightValue( x, y, z ) - 6;
		}

		return 0;
	}

	@Override
	public void onNeighborChange( final IBlockAccess world, final int x, final int y, final int z, final int tileX, final int tileY, final int tileZ )
	{
		super.onNeighborChange( world, x, y, z, tileX, tileY, tileZ );

		final TileLightDetector tld = this.getTileEntity( world, x, y, z );
		if( tld != null )
		{
			tld.updateLight();
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( final World w, final int x, final int y, final int z, final Random r )
	{
		// cancel out lightning
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderQuartzTorch getRenderer()
	{
		return new RenderQuartzTorch();
	}

	@Override
	public boolean isValidOrientation( final World w, final int x, final int y, final int z, final ForgeDirection forward, final ForgeDirection up )
	{
		return this.canPlaceAt( w, x, y, z, up.getOpposite() );
	}

	private boolean canPlaceAt( final World w, final int x, final int y, final int z, final ForgeDirection dir )
	{
		return w.isSideSolid( x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite(), false );
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final int x, final int y, final int z, final Entity e, final boolean isVisual )
	{
		final ForgeDirection up = this.getOrientable( w, x, y, z ).getUp();
		final double xOff = -0.3 * up.offsetX;
		final double yOff = -0.3 * up.offsetY;
		final double zOff = -0.3 * up.offsetZ;
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List out, final Entity e )
	{/*
	 * double xOff = -0.15 * getUp().offsetX; double yOff = -0.15 * getUp().offsetY; double zOff = -0.15 *
	 * getUp().offsetZ; out.add( AxisAlignedBB.getBoundingBox( xOff + (double) x + 0.15, yOff + (double) y + 0.15, zOff
	 * + (double) z + 0.15,// ahh xOff + (double) x + 0.85, yOff + (double) y + 0.85, zOff + (double) z + 0.85 ) );
	 */
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block id )
	{
		final ForgeDirection up = this.getOrientable( w, x, y, z ).getUp();
		if( !this.canPlaceAt( w, x, y, z, up.getOpposite() ) )
		{
			this.dropTorch( w, x, y, z );
		}
	}

	private void dropTorch( final World w, final int x, final int y, final int z )
	{
		w.func_147480_a( x, y, z, true );
		// w.destroyBlock( x, y, z, true );
		w.markBlockForUpdate( x, y, z );
	}

	@Override
	public boolean canPlaceBlockAt( final World w, final int x, final int y, final int z )
	{
		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
		{
			if( this.canPlaceAt( w, x, y, z, dir ) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w, final int x, final int y, final int z )
	{
		return new MetaRotation( w, x, y, z );
	}
}
