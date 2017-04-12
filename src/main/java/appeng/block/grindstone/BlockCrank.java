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


import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockCrank;
import appeng.core.features.AEFeature;
import appeng.core.stats.Stats;
import appeng.tile.AEBaseTile;
import appeng.tile.grindstone.TileCrank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


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
	@SideOnly( Side.CLIENT )
	public RenderBlockCrank getRenderer()
	{
		return new RenderBlockCrank();
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( player instanceof FakePlayer || player == null )
		{
			this.dropCrank( w, x, y, z );
			return true;
		}

		final AEBaseTile tile = this.getTileEntity( w, x, y, z );
		if( tile instanceof TileCrank )
		{
			if( ( (TileCrank) tile ).power() )
			{
				Stats.TurnedCranks.addToPlayer( player, 1 );
			}
		}

		return true;
	}

	private void dropCrank( final World world, final int x, final int y, final int z )
	{
		world.func_147480_a( x, y, z, true ); // w.destroyBlock( x, y, z, true );
		world.markBlockForUpdate( x, y, z );
	}

	@Override
	public void onBlockPlacedBy( final World world, final int x, final int y, final int z, final EntityLivingBase placer, final ItemStack itemStack )
	{
		final AEBaseTile tile = this.getTileEntity( world, x, y, z );
		if( tile != null )
		{
			final ForgeDirection mnt = this.findCrankable( world, x, y, z );
			ForgeDirection forward = ForgeDirection.UP;
			if( mnt == ForgeDirection.UP || mnt == ForgeDirection.DOWN )
			{
				forward = ForgeDirection.SOUTH;
			}
			tile.setOrientation( forward, mnt.getOpposite() );
		}
		else
		{
			this.dropCrank( world, x, y, z );
		}
	}

	@Override
	public boolean isValidOrientation( final World world, final int x, final int y, final int z, final ForgeDirection forward, final ForgeDirection up )
	{
		final TileEntity te = world.getTileEntity( x, y, z );
		return !( te instanceof TileCrank ) || this.isCrankable( world, x, y, z, up.getOpposite() );
	}

	private ForgeDirection findCrankable( final World world, final int x, final int y, final int z )
	{
		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
		{
			if( this.isCrankable( world, x, y, z, dir ) )
			{
				return dir;
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	private boolean isCrankable( final World world, final int x, final int y, final int z, final ForgeDirection offset )
	{
		final TileEntity te = world.getTileEntity( x + offset.offsetX, y + offset.offsetY, z + offset.offsetZ );

		return te instanceof ICrankable && ( (ICrankable) te ).canCrankAttach( offset.getOpposite() );
	}

	@Override
	public void onNeighborBlockChange( final World world, final int x, final int y, final int z, final Block block )
	{
		final AEBaseTile tile = this.getTileEntity( world, x, y, z );
		if( tile != null )
		{
			if( !this.isCrankable( world, x, y, z, tile.getUp().getOpposite() ) )
			{
				this.dropCrank( world, x, y, z );
			}
		}
		else
		{
			this.dropCrank( world, x, y, z );
		}
	}

	@Override
	public boolean canPlaceBlockAt( final World world, final int x, final int y, final int z )
	{
		return this.findCrankable( world, x, y, z ) != ForgeDirection.UNKNOWN;
	}
}
