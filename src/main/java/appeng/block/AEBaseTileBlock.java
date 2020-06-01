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

package appeng.block;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseInvTile;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public abstract class AEBaseTileBlock extends AEBaseBlock implements ITileEntityProvider
{

	@Nonnull
	private Class<? extends AEBaseTile> tileEntityType;

	public AEBaseTileBlock( final Block.Properties props )
	{
		super( props );
	}
// FIXME
//	public static final UnlistedDirection FORWARD = new UnlistedDirection( "forward" );
//	public static final UnlistedDirection UP = new UnlistedDirection( "up" );

	@Override
	public BlockState getExtendedState( BlockState state, IBlockReader world, BlockPos pos )
	{
// FIXME
//		// A subclass may decide it doesn't want extended block state for whatever reason
//		if( !( state instanceof IExtendedBlockState ) )
//		{
//			return state;
//		}
//
//		AEBaseTile tile = this.getTileEntity( world, pos );
//		if( tile == null )
//		{
//			return state; // No info available
//		}
//
//		IExtendedBlockState extState = (IExtendedBlockState) state;
//		return extState.with( FORWARD, tile.getForward() ).with( UP, tile.getUp() );
		throw new IllegalStateException();
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);

		// TODO: Rendering stuff

//		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] {
//				FORWARD,
//				UP
//		} );
	}

	// TODO : Was this change needed?
	public void setTileEntity( final Class<? extends AEBaseTile> c )
	{
		this.tileEntityType = c;
		this.setInventory( AEBaseInvTile.class.isAssignableFrom( c ) );
	}

	@Override
	public boolean hasTileEntity( BlockState state )
	{
		return this.hasBlockTileEntity();
	}

	private boolean hasBlockTileEntity()
	{
		return this.tileEntityType != null;
	}

	public Class<? extends AEBaseTile> getTileEntityClass()
	{
		return this.tileEntityType;
	}

	@Nullable
	public <T extends AEBaseTile> T getTileEntity( final IBlockReader w, final int x, final int y, final int z )
	{
		return this.getTileEntity( w, new BlockPos( x, y, z ) );
	}

	@Nullable
	public <T extends AEBaseTile> T getTileEntity( final IBlockReader w, final BlockPos pos )
	{
		if( !this.hasBlockTileEntity() )
		{
			return null;
		}

		final TileEntity te = w.getTileEntity( pos );
		if( this.tileEntityType.isInstance( te ) )
		{
			return (T) te;
		}

		return null;
	}

	@Override
	public final TileEntity createTileEntity( BlockState state, IBlockReader world )
	{
		if( this.hasBlockTileEntity() )
		{
			try
			{
				return this.tileEntityType.newInstance();
			}
			catch( final InstantiationException e )
			{
				throw new IllegalStateException( "Failed to create a new instance of an illegal class " + this.tileEntityType, e );
			}
			catch( final IllegalAccessException e )
			{
				throw new IllegalStateException( "Failed to create a new instance of " + this.tileEntityType + ", because lack of permissions", e );
			}
		}

		return null;
	}

	@Override
	public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {

		final AEBaseTile te = this.getTileEntity( w, pos );
		if( te != null )
		{
			final ArrayList<ItemStack> drops = new ArrayList<>();
			if( te.dropItems() )
			{
				te.getDrops( w, pos, drops );
			}
			else
			{
				te.getNoDrops( w, pos, drops );
			}

			// Cry ;_; ...
			Platform.spawnDrops( w, pos, drops );
		}

		// super will remove the TE, as it is not an instance of BlockContainer
		super.onReplaced( state, w, pos, newState, isMoving );
	}

	@Override
	public Direction[] getValidRotations(BlockState state, IBlockReader world, BlockPos pos)
	{
		final AEBaseTile obj = this.getTileEntity( world, pos );
		if( obj != null && obj.canBeRotated() )
		{
			return Direction.values();
		}

		return super.getValidRotations( state, world, pos );
	}

	@Override
	public boolean recolorBlock( BlockState state, final IWorld world, final BlockPos pos, final Direction side, final DyeColor color )
	{
		final TileEntity te = this.getTileEntity( world, pos );

		if( te instanceof IColorableTile )
		{
			final IColorableTile ct = (IColorableTile) te;
			final AEColor c = ct.getColor();
			final AEColor newColor = AEColor.values()[color.ordinal()];

			if( c != newColor )
			{
				ct.recolourBlock( side, newColor, null );
				return true;
			}
			return false;
		}

		return super.recolorBlock( state, world, pos, side, color );
	}

	@Override
	public int getComparatorInputOverride( BlockState state, final World w, final BlockPos pos )
	{
		final TileEntity te = this.getTileEntity( w, pos );
		if( te instanceof AEBaseInvTile )
		{
			AEBaseInvTile invTile = (AEBaseInvTile) te;
			if( invTile.getInternalInventory().getSlots() > 0 )
			{
				return ItemHandlerHelper.calcRedstoneFromInventory( invTile.getInternalInventory() );
			}
		}
		return 0;
	}

	@Override
	public boolean eventReceived( final BlockState state, final World worldIn, final BlockPos pos, final int eventID, final int eventParam )
	{
		super.eventReceived( state, worldIn, pos, eventID, eventParam );
		final TileEntity tileentity = worldIn.getTileEntity( pos );
		return tileentity != null ? tileentity.receiveClientEvent( eventID, eventParam ) : false;
	}

	@Override
	public void onBlockPlacedBy( final World w, final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack is )
	{
		if( is.hasDisplayName() )
		{
			final TileEntity te = this.getTileEntity( w, pos );
			if( te instanceof AEBaseTile )
			{
				// FIXME: Check if this will make it translated
				( (AEBaseTile) w.getTileEntity( pos ) ).setName( is.getDisplayName().getString() );
			}
		}
	}

	@Override
	public ActionResultType onBlockActivated( BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit )
	{
		ItemStack heldItem;
		if( player != null && !player.getHeldItem( hand ).isEmpty() )
		{
			heldItem = player.getHeldItem( hand );

			if( Platform.isWrench( player, heldItem, pos ) && player.isCrouching() )
			{
				final BlockState blockState = world.getBlockState( pos );
				final Block block = blockState.getBlock();

				final AEBaseTile tile = this.getTileEntity( world, pos );

				if( tile == null )
				{
					return ActionResultType.FAIL;
				}

				// FIXME if( tile instanceof TileCableBus || tile instanceof TileSkyChest )
				// {
				// 	return ActionResultType.FAIL;
				// }

				final ItemStack[] itemDropCandidates = Platform.getBlockDrops( world, pos );
				final ItemStack op = new ItemStack( this );

				for( final ItemStack ol : itemDropCandidates )
				{
					if( Platform.itemComparisons().isEqualItemType( ol, op ) )
					{
						final CompoundNBT tag = tile.downloadSettings( SettingsFrom.DISMANTLE_ITEM );
						if( tag != null )
						{
							ol.setTag( tag );
						}
					}
				}

				if( block.removedByPlayer( blockState, world, pos, player, false, world.getFluidState(pos) ) )
				{
					final List<ItemStack> itemsToDrop = Lists.newArrayList( itemDropCandidates );
					Platform.spawnDrops( world, pos, itemsToDrop );
					world.removeBlock(pos, false);
				}

				return ActionResultType.FAIL;
			}

			if( heldItem.getItem() instanceof IMemoryCard /* FIXME && !( this instanceof BlockCableBus ) */ )
			{
				final IMemoryCard memoryCard = (IMemoryCard) heldItem.getItem();
				final AEBaseTile tileEntity = this.getTileEntity( world, pos );

				if( tileEntity == null )
				{
					return ActionResultType.FAIL;
				}

				final String name = this.getTranslationKey();

				if( player.isCrouching() )
				{
					final CompoundNBT data = tileEntity.downloadSettings( SettingsFrom.MEMORY_CARD );
					if( data != null )
					{
						memoryCard.setMemoryCardContents( heldItem, name, data );
						memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
					}
				}
				else
				{
					final String savedName = memoryCard.getSettingsName( heldItem );
					final CompoundNBT data = memoryCard.getData( heldItem );

					if( this.getTranslationKey().equals( savedName ) )
					{
						tileEntity.uploadSettings( SettingsFrom.MEMORY_CARD, data );
						memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
					}
					else
					{
						memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
					}
				}

				return ActionResultType.SUCCESS;
			}
		}

		return this.onActivated( world, pos, player, hand, player.getHeldItem( hand ), hit );
	}

	@Override
	public IOrientable getOrientable(final IBlockReader w, final BlockPos pos )
	{
		return this.getTileEntity( w, pos );
	}

	@Override
	public ICustomCollision getCustomCollision( final IBlockReader w, final BlockPos pos )
	{
		final AEBaseTile te = this.getTileEntity( w, pos );
		if( te instanceof ICustomCollision )
		{
			return (ICustomCollision) te;
		}

		return super.getCustomCollision( w, pos );
	}

}
