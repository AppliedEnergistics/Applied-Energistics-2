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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCrank;
import appeng.core.features.AEFeature;
import appeng.core.stats.Stats;
import appeng.tile.AEBaseTile;
import appeng.tile.grindstone.TileCrank;


public class BlockCrank extends AEBaseTileBlock
{

	public BlockCrank()
	{
		super( Material.wood );

		this.setTileEntity( TileCrank.class );
		this.setLightOpacity( 0 );
		this.setHarvestLevel( "axe", 0 );
		this.isFullSize = this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.GrindStone ) );
	}

	@Override
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCrank.class;
	}
	
	@Override
	public boolean onActivated(
			World w,
			BlockPos pos,
			EntityPlayer player,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ )
	{
		if( player instanceof FakePlayer || player == null )
		{
			this.dropCrank( w, pos );
			return true;
		}

		AEBaseTile tile = this.getTileEntity( w, pos );
		if( tile instanceof TileCrank )
		{
			if( ( (TileCrank) tile ).power() )
			{
				Stats.TurnedCranks.addToPlayer( player, 1 );
			}
		}

		return true;
	}

	private void dropCrank( World world, BlockPos pos )
	{
		world.destroyBlock( pos, true ); // w.destroyBlock( x, y, z, true );
		world.markBlockForUpdate( pos );
	}
		
	@Override
	public void onBlockPlacedBy(
			World world,
			BlockPos pos,
			IBlockState state,
			EntityLivingBase placer,
			ItemStack stack )
	{
		AEBaseTile tile = this.getTileEntity( world, pos );
		if( tile != null )
		{
			EnumFacing mnt = this.findCrankable( world, pos );
			EnumFacing forward = EnumFacing.UP;
			if( mnt == EnumFacing.UP || mnt == EnumFacing.DOWN )
			{
				forward = EnumFacing.SOUTH;
			}
			tile.setOrientation( forward, mnt.getOpposite() );
		}
		else
		{
			this.dropCrank( world, pos );
		}
	}
	
	@Override
	public boolean isValidOrientation(
			World w,
			BlockPos pos,
			EnumFacing forward,
			EnumFacing up )
	{
		TileEntity te = w.getTileEntity( pos );
		return !( te instanceof TileCrank ) || this.isCrankable( w, pos, up.getOpposite() );
	}

	private EnumFacing findCrankable( World world, BlockPos pos )
	{
		for( EnumFacing dir : EnumFacing.VALUES )
		{
			if( this.isCrankable( world, pos, dir ) )
			{
				return dir;
			}
		}
		return null;
	}

	private boolean isCrankable( World world, BlockPos pos, EnumFacing offset )
	{
		BlockPos o = pos.offset( offset);
		TileEntity te = world.getTileEntity( o );

		return te instanceof ICrankable && ( (ICrankable) te ).canCrankAttach( offset.getOpposite() );
	}
	
	@Override
	public void onNeighborBlockChange(
			World world,
			BlockPos pos,
			IBlockState state,
			Block neighborBlock )
	{

		AEBaseTile tile = this.getTileEntity( world, pos );
		if( tile != null )
		{
			if( !this.isCrankable( world, pos, tile.getUp().getOpposite() ) )
			{
				this.dropCrank( world, pos );
			}
		}
		else
		{
			this.dropCrank( world, pos );
		}
	}

	@Override
	public boolean canPlaceBlockAt( World world, BlockPos pos )
	{
		return this.findCrankable( world, pos ) != null;
	}
}
