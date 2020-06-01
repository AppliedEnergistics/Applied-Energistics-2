/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.items.parts;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.parts.IAlphaPassItem;
import appeng.api.util.AEPartLocation;
import appeng.core.AELog;
import appeng.core.FacadeConfig;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.items.AEBaseItem;


public class ItemFacade extends AEBaseItem implements IFacadeItem, IAlphaPassItem
{

	private static final String TAG_ITEM_ID = "item";
	private static final String TAG_DAMAGE = "damage";

	private List<ItemStack> subTypes = null;

	public ItemFacade()
	{
		this.setHasSubtypes( true );
	}

	@Override
	public EnumActionResult onItemUseFirst( final PlayerEntity player, final World world, final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ, final Hand hand )
	{
		return Api.INSTANCE.partHelper().placeBus( player.getHeldItem( hand ), pos, side, player, hand, world );
	}

	@Override
	public String getItemStackDisplayName( final ItemStack is )
	{
		try
		{
			final ItemStack in = this.getTextureItem( is );
			if( !in.isEmpty() )
			{
				return super.getItemStackDisplayName( is ) + " - " + in.getDisplayName();
			}
		}
		catch( final Throwable ignored )
		{

		}

		return super.getItemStackDisplayName( is );
	}

	@Override
	protected void getCheckedSubItems( final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks )
	{
		this.calculateSubTypes();
		itemStacks.addAll( this.subTypes );
	}

	private void calculateSubTypes()
	{
		if( this.subTypes == null )
		{
			this.subTypes = new ArrayList<>( 1000 );
			for( final Object blk : Block.REGISTRY )
			{
				final Block b = (Block) blk;
				try
				{
					final Item item = Item.getItemFromBlock( b );
					if( item == Items.AIR )
					{
						continue;
					}

					final NonNullList<ItemStack> tmpList = NonNullList.create();
					b.getSubBlocks( b.getCreativeTabToDisplayOn(), tmpList );
					for( final ItemStack l : tmpList )
					{
						final ItemStack facade = this.createFacadeForItem( l, false );
						if( !facade.isEmpty() )
						{
							this.subTypes.add( facade );
						}
					}
				}
				catch( final Throwable t )
				{
					// just absorb..
				}
			}
		}
	}

	private static boolean hasSimpleModel( BlockState blockState )
	{
		if( blockState.getRenderType() != EnumBlockRenderType.MODEL || blockState instanceof IExtendedBlockState )
		{
			return false;
		}

		return blockState.isFullCube();
	}

	public ItemStack createFacadeForItem( final ItemStack itemStack, final boolean returnItem )
	{
		if( itemStack.isEmpty() )
		{
			return ItemStack.EMPTY;
		}

		final Block block = Block.getBlockFromItem( itemStack.getItem() );
		if( block == Blocks.AIR || itemStack.hasTag() )
		{
			return ItemStack.EMPTY;
		}

		final int metadata = itemStack.getItem().getMetadata( itemStack.getDamage() );

		// Try to get the block state based on the item stack's meta. If this fails, don't consider it for a facade
		// This for example fails for Pistons because they hardcoded an invalid meta value in vanilla
		BlockState blockState;
		try
		{
			blockState = block.getStateFromMeta( metadata );
		}
		catch( Exception e )
		{
			AELog.debug( e, "Cannot create a facade for " + block.getRegistryName() );
			return ItemStack.EMPTY;
		}

		final boolean areTileEntitiesEnabled = FacadeConfig.instance().allowTileEntityFacades();
		final boolean isWhiteListed = FacadeConfig.instance().isWhiteListed( block, metadata );
		final boolean isModel = blockState.getRenderType() == EnumBlockRenderType.MODEL;

		final BlockState defaultState = block.getDefaultState();
		final boolean isTileEntity = block.hasTileEntity( defaultState );
		final boolean isFullCube = block.isFullCube( defaultState );

		final boolean isTileEntityAllowed = !isTileEntity || ( areTileEntitiesEnabled && isWhiteListed );
		final boolean isBlockAllowed = isFullCube || isWhiteListed;

		if( isModel && isTileEntityAllowed && isBlockAllowed )
		{
			if( returnItem )
			{
				return itemStack;
			}

			final ItemStack is = new ItemStack( this );
			final CompoundNBT data = new CompoundNBT();
			data.putString(TAG_ITEM_ID, itemStack.getItem().getRegistryName().toString());
			data.setInteger( TAG_DAMAGE, itemStack.getDamage() );
			is.setTagCompound( data );
			return is;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public FacadePart createPartFromItemStack( final ItemStack is, final AEPartLocation side )
	{
		final ItemStack in = this.getTextureItem( is );
		if( !in.isEmpty() )
		{
			return new FacadePart( is, side );
		}
		return null;
	}

	@Override
	public ItemStack getTextureItem( ItemStack is )
	{

		CompoundNBT nbt = is.getTag();

		if( nbt == null )
		{
			return ItemStack.EMPTY;
		}

		ResourceLocation itemId;
		int itemDamage;

		// Handle legacy facades
		if( nbt.contains("x") )
		{
			int[] data = nbt.getIntArray( "x" );
			if( data.length != 2 )
			{
				return ItemStack.EMPTY;
			}

			Item item = Item.REGISTRY.getObjectById( data[0] );
			if( item == null )
			{
				return ItemStack.EMPTY;
			}

			itemId = item.getRegistryName();
			itemDamage = data[1];
		}
		else
		{
			// First item is numeric item id, second is damage
			itemId = new ResourceLocation( nbt.getString( TAG_ITEM_ID ) );
			itemDamage = nbt.getInteger( TAG_DAMAGE );
		}

		Item baseItem = Item.REGISTRY.getObject( itemId );

		if( baseItem == null )
		{
			return ItemStack.EMPTY;
		}

		return new ItemStack( baseItem, 1, itemDamage );
	}

	@Override
	public BlockState getTextureBlockState( ItemStack is )
	{

		ItemStack baseItemStack = this.getTextureItem( is );

		if( baseItemStack.isEmpty() )
		{
			return Blocks.GLASS.getDefaultState();
		}

		Block block = Block.getBlockFromItem( baseItemStack.getItem() );

		if( block == Blocks.AIR )
		{
			return Blocks.GLASS.getDefaultState();
		}

		int metadata = baseItemStack.getItem().getMetadata( baseItemStack );

		try
		{
			return block.getStateFromMeta( metadata );
		}
		catch( Exception e )
		{
			AELog.warn( "Block %s has broken getStateFromMeta method for meta %d", block.getRegistryName().toString(), baseItemStack.getDamage() );
			return Blocks.GLASS.getDefaultState();
		}
	}

	public List<ItemStack> getFacades()
	{
		this.calculateSubTypes();
		return this.subTypes;
	}

	public ItemStack getCreativeTabIcon()
	{
		this.calculateSubTypes();
		if( this.subTypes.isEmpty() )
		{
			return new ItemStack( Items.CAKE );
		}
		return this.subTypes.get( 0 );
	}

	public ItemStack createFromIDs( final int[] ids )
	{
		ItemStack facadeStack = Api.INSTANCE
				.definitions()
				.items()
				.facade()
				.maybeStack( 1 )
				.orElseThrow( () -> new MissingDefinitionException( "Tried to create a facade, while facades are being deactivated." ) );

		// Convert back to a registry name...
		Item item = Item.REGISTRY.getObjectById( ids[0] );
		if( item == null )
		{
			return ItemStack.EMPTY;
		}

		final CompoundNBT facadeTag = new CompoundNBT();
		facadeTag.putString(TAG_ITEM_ID, item.getRegistryName().toString());
		facadeTag.setInteger( TAG_DAMAGE, ids[1] );
		facadeStack.setTagCompound( facadeTag );

		return facadeStack;
	}

	@Override
	public boolean useAlphaPass( final ItemStack is )
	{
		BlockState blockState = this.getTextureBlockState( is );

		if( blockState == null )
		{
			return false;
		}

		Block blk = blockState.getBlock();
		return blk.canRenderInLayer( blockState, BlockRenderLayer.TRANSLUCENT );
	}
}
