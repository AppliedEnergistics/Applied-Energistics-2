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

import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.block.networking.BlockCableBus;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseInvTile;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public abstract class AEBaseTileBlock extends AEBaseBlock implements ITileEntityProvider
{

	@Nonnull
	private Class<? extends AEBaseTile> tileEntityType;

	public AEBaseTileBlock( final Material mat )
	{
		super( mat );
	}

	public static final UnlistedDirection FORWARD = new UnlistedDirection( "forward" );
	public static final UnlistedDirection UP = new UnlistedDirection( "up" );

	@Override
	public IBlockState getExtendedState( IBlockState state, IBlockAccess world, BlockPos pos )
	{
		// A subclass may decide it doesn't want extended block state for whatever reason
		if( !( state instanceof IExtendedBlockState ) )
		{
			return state;
		}

		AEBaseTile tile = this.getTileEntity( world, pos );
		if( tile == null )
		{
			return state; // No info available
		}

		IExtendedBlockState extState = (IExtendedBlockState) state;
		return extState.withProperty( FORWARD, tile.getForward() ).withProperty( UP, tile.getUp() );
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] {
				FORWARD,
				UP
		} );
	}

	@Override
	public int getMetaFromState( IBlockState state )
	{
		return 0;
	}

	// TODO : Was this change needed?
	public void setTileEntity( final Class<? extends AEBaseTile> c )
	{
		this.tileEntityType = c;
		this.setInventory( AEBaseInvTile.class.isAssignableFrom( c ) );
	}

	@Override
	public boolean hasTileEntity( IBlockState state )
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
	public <T extends AEBaseTile> T getTileEntity( final IBlockAccess w, final int x, final int y, final int z )
	{
		return this.getTileEntity( w, new BlockPos( x, y, z ) );
	}

	@Nullable
	public <T extends AEBaseTile> T getTileEntity( final IBlockAccess w, final BlockPos pos )
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
	public final TileEntity createNewTileEntity( final World var1, final int var2 )
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
	public void breakBlock( final World w, final BlockPos pos, final IBlockState state )
	{
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
		super.breakBlock( w, pos, state );
	}

	@Override
	public final EnumFacing[] getValidRotations( final World w, final BlockPos pos )
	{
		final AEBaseTile obj = this.getTileEntity( w, pos );
		if( obj != null && obj.canBeRotated() )
		{
			return EnumFacing.VALUES;
		}

		return super.getValidRotations( w, pos );
	}

	@Override
	public boolean recolorBlock( final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color )
	{
		final TileEntity te = this.getTileEntity( world, pos );

		if( te instanceof IColorableTile )
		{
			final IColorableTile ct = (IColorableTile) te;
			final AEColor c = ct.getColor();
			final AEColor newColor = AEColor.values()[color.getMetadata()];

			if( c != newColor )
			{
				ct.recolourBlock( side, newColor, null );
				return true;
			}
			return false;
		}

		return super.recolorBlock( world, pos, side, color );
	}

	@Override
	public int getComparatorInputOverride( IBlockState state, final World w, final BlockPos pos )
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
	public boolean eventReceived( final IBlockState state, final World worldIn, final BlockPos pos, final int eventID, final int eventParam )
	{
		super.eventReceived( state, worldIn, pos, eventID, eventParam );
		final TileEntity tileentity = worldIn.getTileEntity( pos );
		return tileentity != null ? tileentity.receiveClientEvent( eventID, eventParam ) : false;
	}

	@Override
	public void onBlockPlacedBy( final World w, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack is )
	{
		if( is.hasDisplayName() )
		{
			final TileEntity te = this.getTileEntity( w, pos );
			if( te instanceof AEBaseTile )
			{
				( (AEBaseTile) w.getTileEntity( pos ) ).setName( is.getDisplayName() );
			}
		}
	}

	@Override
	public boolean onBlockActivated( World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ )
	{
		ItemStack heldItem;
		if( player != null && !player.getHeldItem( hand ).isEmpty() )
		{
			heldItem = player.getHeldItem( hand );

			if( Platform.isWrench( player, heldItem, pos ) && player.isSneaking() )
			{
				final IBlockState blockState = world.getBlockState( pos );
				final Block block = blockState.getBlock();

				if( block == null )
				{
					return false;
				}

				final AEBaseTile tile = this.getTileEntity( world, pos );

				if( tile == null )
				{
					return false;
				}

				if( tile instanceof TileCableBus || tile instanceof TileSkyChest )
				{
					return false;
				}

				final ItemStack[] itemDropCandidates = Platform.getBlockDrops( world, pos );
				final ItemStack op = new ItemStack( this );

				for( final ItemStack ol : itemDropCandidates )
				{
					if( Platform.itemComparisons().isEqualItemType( ol, op ) )
					{
						final NBTTagCompound tag = tile.downloadSettings( SettingsFrom.DISMANTLE_ITEM );
						if( tag != null )
						{
							ol.setTagCompound( tag );
						}
					}
				}

				if( block.removedByPlayer( blockState, world, pos, player, false ) )
				{
					final List<ItemStack> itemsToDrop = Lists.newArrayList( itemDropCandidates );
					Platform.spawnDrops( world, pos, itemsToDrop );
					world.setBlockToAir( pos );
				}

				return false;
			}

			if( heldItem.getItem() instanceof IMemoryCard && !( this instanceof BlockCableBus ) )
			{
				final IMemoryCard memoryCard = (IMemoryCard) heldItem.getItem();
				final AEBaseTile tileEntity = this.getTileEntity( world, pos );

				if( tileEntity == null )
				{
					return false;
				}

				final String name = this.getUnlocalizedName();

				if( player.isSneaking() )
				{
					final NBTTagCompound data = tileEntity.downloadSettings( SettingsFrom.MEMORY_CARD );
					if( data != null )
					{
						memoryCard.setMemoryCardContents( heldItem, name, data );
						memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
					}
				}
				else
				{
					final String savedName = memoryCard.getSettingsName( heldItem );
					final NBTTagCompound data = memoryCard.getData( heldItem );

					if( this.getUnlocalizedName().equals( savedName ) )
					{
						tileEntity.uploadSettings( SettingsFrom.MEMORY_CARD, data );
						memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
					}
					else
					{
						memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
					}
				}

				return true;
			}

			if (heldItem.getItem() instanceof ToolQuartzCuttingKnife && !(this instanceof BlockCableBus)) {
				if (ForgeEventFactory.onItemUseStart(player, heldItem, 1) <= 0) return false;
				final AEBaseTile tile = this.getTileEntity(world, pos);
				if (tile == null) return false;
				Platform.openGUI(player, tile, AEPartLocation.fromFacing(facing), GuiBridge.GUI_RENAMER);
				return true;
			}
		}

		return this.onActivated( world, pos, player, hand, player.getHeldItem( hand ), facing, hitX, hitY, hitZ );
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w, final BlockPos pos )
	{
		return this.getTileEntity( w, pos );
	}

	@Override
	public ICustomCollision getCustomCollision( final World w, final BlockPos pos )
	{
		final AEBaseTile te = this.getTileEntity( w, pos );
		if( te instanceof ICustomCollision )
		{
			return (ICustomCollision) te;
		}

		return super.getCustomCollision( w, pos );
	}

}
