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


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseTileBlock;
import appeng.core.stats.Stats;
import appeng.tile.AEBaseTile;
import appeng.tile.grindstone.TileCrank;


public class BlockCrank extends AEBaseTileBlock
{

	public BlockCrank()
	{
		super( Material.WOOD );

		this.setLightOpacity( 0 );
		this.setHarvestLevel( "axe", 0 );
		this.setFullSize( this.setOpaque( false ) );
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final PlayerEntity player, final Hand hand, final @Nullable ItemStack heldItem, final Direction side, final float hitX, final float hitY, final float hitZ )
	{
		if( player instanceof FakePlayer || player == null )
		{
			this.dropCrank( w, pos );
			return true;
		}

		final AEBaseTile tile = this.getTileEntity( w, pos );
		if( tile instanceof TileCrank )
		{
			if( ( (TileCrank) tile ).power() )
			{
				Stats.TurnedCranks.addToPlayer( player, 1 );
			}
		}

		return true;
	}

	private void dropCrank( final World world, final BlockPos pos )
	{
		world.destroyBlock( pos, true ); // w.destroyBlock( x, y, z, true );
		world.notifyBlockUpdate( pos, this.getDefaultState(), world.getBlockState( pos ), 3 );
	}

	@Override
	public void onBlockPlacedBy( final World world, final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack )
	{
		final AEBaseTile tile = this.getTileEntity( world, pos );
		if( tile != null )
		{
			final Direction mnt = this.findCrankable( world, pos );
			Direction forward = Direction.UP;
			if( mnt == Direction.UP || mnt == Direction.DOWN )
			{
				forward = Direction.SOUTH;
			}
			tile.setOrientation( forward, mnt.getOpposite() );
		}
		else
		{
			this.dropCrank( world, pos );
		}
	}

	@Override
	public boolean isValidOrientation( final World w, final BlockPos pos, final Direction forward, final Direction up )
	{
		final TileEntity te = w.getTileEntity( pos );
		return !( te instanceof TileCrank ) || this.isCrankable( w, pos, up.getOpposite() );
	}

	private Direction findCrankable( final World world, final BlockPos pos )
	{
		for( final Direction dir : Direction.VALUES )
		{
			if( this.isCrankable( world, pos, dir ) )
			{
				return dir;
			}
		}
		return null;
	}

	private boolean isCrankable( final World world, final BlockPos pos, final Direction offset )
	{
		final BlockPos o = pos.offset( offset );
		final TileEntity te = world.getTileEntity( o );

		return te instanceof ICrankable && ( (ICrankable) te ).canCrankAttach( offset.getOpposite() );
	}

	@Override
	public EnumBlockRenderType getRenderType( BlockState state )
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public void neighborChanged( BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos )
	{

		final AEBaseTile tile = this.getTileEntity( world, pos );
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
	public boolean canPlaceBlockAt( final World world, final BlockPos pos )
	{
		return this.findCrankable( world, pos ) != null;
	}

	@Override
	public boolean isFullCube( BlockState state )
	{
		return false;
	}

	@Override
	public boolean canPlaceTorchOnTop( BlockState state, IBlockReader world, BlockPos pos )
	{
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape( IBlockReader worldIn, BlockState state, BlockPos pos, Direction face )
	{
		return BlockFaceShape.UNDEFINED;
	}

}
