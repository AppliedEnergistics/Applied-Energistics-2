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


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderQuartzTorch;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.helpers.MetaRotation;
import appeng.tile.misc.TileLightDetector;


public class BlockLightDetector extends AEBaseTileBlock implements IOrientableBlock,ICustomCollision
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
	public int getMetaFromState(
			IBlockState state )
	{
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(
			int meta )
	{
		return getDefaultState();
	}
	
	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[]{ BlockTorch.FACING };
	}

	@Override
	public int isProvidingWeakPower(
			IBlockAccess w,
			BlockPos pos,
			IBlockState state,
			EnumFacing side )
	{
		if( w instanceof World && ( (TileLightDetector) this.getTileEntity( w, pos ) ).isReady() )
		{
			return ( (World) w ).getLightFromNeighbors( pos ) - 6;
		}

		return 0;
	}

	@Override
	public void onNeighborChange(
			IBlockAccess world,
			BlockPos pos,
			BlockPos neighbor )
	{
		super.onNeighborChange( world, pos, neighbor );

		TileLightDetector tld = this.getTileEntity( world, pos );
		if( tld != null )
		{
			tld.updateLight();
		}
	}

	@Override
	public void randomDisplayTick(
			World worldIn,
			BlockPos pos,
			IBlockState state,
			Random rand )
	{
		// cancel out lightning
	}

	@Override
	protected Class<? extends RenderQuartzTorch> getRenderer()
	{
		return RenderQuartzTorch.class;
	}

	@Override
	public boolean isValidOrientation(
			World w,
			BlockPos pos,
			EnumFacing forward,
			EnumFacing up )
	{
		return this.canPlaceAt( w, pos, up.getOpposite() );
	}

	private boolean canPlaceAt( World w, BlockPos pos, EnumFacing dir )
	{
		return w.isSideSolid( pos.offset( dir ), dir.getOpposite(), false );
	}
	
	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(
			World w,
			BlockPos pos,
			Entity thePlayer,
			boolean b )
	{
		EnumFacing up = this.getOrientable( w, pos ).getUp();
		double xOff = -0.3 * up.getFrontOffsetX();
		double yOff = -0.3 * up.getFrontOffsetY();
		double zOff = -0.3 * up.getFrontOffsetZ();
		return Collections.singletonList( AxisAlignedBB.fromBounds( xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7 ) );
	}

	@Override
	public void addCollidingBlockToList(
			World w,
			BlockPos pos,
			AxisAlignedBB bb,
			List<AxisAlignedBB> out,
			Entity e )
	{/*
	 * double xOff = -0.15 * getUp().offsetX; double yOff = -0.15 * getUp().offsetY; double zOff = -0.15 *
	 * getUp().offsetZ; out.add( AxisAlignedBB.getBoundingBox( xOff + (double) x + 0.15, yOff + (double) y + 0.15, zOff
	 * + (double) z + 0.15,// ahh xOff + (double) x + 0.85, yOff + (double) y + 0.85, zOff + (double) z + 0.85 ) );
	 */
	}
	
	@Override
	public void onNeighborBlockChange(
			World w,
			BlockPos pos,
			IBlockState state,
			Block neighborBlock )
	{
		EnumFacing up = this.getOrientable( w, pos ).getUp();
		if( !this.canPlaceAt( w, pos, up.getOpposite() ) )
		{
			this.dropTorch( w, pos );
		}
	}

	private void dropTorch( World w, BlockPos pos )
	{
		w.destroyBlock( pos, true );
		w.markBlockForUpdate( pos );
	}
	
	@Override
	public boolean canPlaceBlockAt(
			World w,
			BlockPos pos )
	{
		for( EnumFacing dir : EnumFacing.VALUES )
		{
			if( this.canPlaceAt( w, pos, dir ) )
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
	public IOrientable getOrientable( final IBlockAccess w, BlockPos pos )
	{
		return new MetaRotation( w, pos,true );
	}
}
