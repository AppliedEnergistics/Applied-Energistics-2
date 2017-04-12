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

package appeng.core.sync.packets;


import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.IPartitionList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PacketNEIRecipe extends AppEngPacket
{

	private ItemStack[][] recipe;

	// automatic.
	public PacketNEIRecipe( final ByteBuf stream ) throws IOException
	{
		final ByteArrayInputStream bytes = new ByteArrayInputStream( stream.array() );
		bytes.skip( stream.readerIndex() );
		final NBTTagCompound comp = CompressedStreamTools.readCompressed( bytes );
		if( comp != null )
		{
			this.recipe = new ItemStack[9][];
			for( int x = 0; x < this.recipe.length; x++ )
			{
				final NBTTagList list = comp.getTagList( "#" + x, 10 );
				if( list.tagCount() > 0 )
				{
					this.recipe[x] = new ItemStack[list.tagCount()];
					for( int y = 0; y < list.tagCount(); y++ )
					{
						this.recipe[x][y] = ItemStack.loadItemStackFromNBT( list.getCompoundTagAt( y ) );
					}
				}
			}
		}
	}

	// api
	public PacketNEIRecipe( final NBTTagCompound recipe ) throws IOException
	{
		final ByteBuf data = Unpooled.buffer();

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream outputStream = new DataOutputStream( bytes );

		data.writeInt( this.getPacketID() );

		CompressedStreamTools.writeCompressed( recipe, outputStream );
		data.writeBytes( bytes.toByteArray() );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		final EntityPlayerMP pmp = (EntityPlayerMP) player;
		final Container con = pmp.openContainer;

		if( con instanceof IContainerCraftingPacket )
		{
			final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
			final IGridNode node = cct.getNetworkNode();
			if( node != null )
			{
				final IGrid grid = node.getGrid();
				if( grid == null )
				{
					return;
				}

				final IStorageGrid inv = grid.getCache( IStorageGrid.class );
				final IEnergyGrid energy = grid.getCache( IEnergyGrid.class );
				final ISecurityGrid security = grid.getCache( ISecurityGrid.class );
				final IInventory craftMatrix = cct.getInventoryByName( "crafting" );
				final IInventory playerInventory = cct.getInventoryByName( "player" );

				final Actionable realForFake = cct.useRealItems() ? Actionable.MODULATE : Actionable.SIMULATE;

				if( inv != null && this.recipe != null && security != null )
				{
					final InventoryCrafting testInv = new InventoryCrafting( new ContainerNull(), 3, 3 );
					for( int x = 0; x < 9; x++ )
					{
						if( this.recipe[x] != null && this.recipe[x].length > 0 )
						{
							testInv.setInventorySlotContents( x, this.recipe[x][0] );
						}
					}

					final IRecipe r = Platform.findMatchingRecipe( testInv, pmp.worldObj );

					if( r != null && security.hasPermission( player, SecurityPermissions.EXTRACT ) )
					{
						final ItemStack is = r.getCraftingResult( testInv );

						if( is != null )
						{
							final IMEMonitor<IAEItemStack> storage = inv.getItemInventory();
							final IItemList all = storage.getStorageList();
							final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter( cct.getViewCells() );

							for( int x = 0; x < craftMatrix.getSizeInventory(); x++ )
							{
								final ItemStack patternItem = testInv.getStackInSlot( x );

								ItemStack currentItem = craftMatrix.getStackInSlot( x );
								if( currentItem != null )
								{
									testInv.setInventorySlotContents( x, currentItem );
									final ItemStack newItemStack = r.matches( testInv, pmp.worldObj ) ? r.getCraftingResult( testInv ) : null;
									testInv.setInventorySlotContents( x, patternItem );

									if( newItemStack == null || !Platform.isSameItemPrecise( newItemStack, is ) )
									{
										final IAEItemStack in = AEItemStack.create( currentItem );
										if( in != null )
										{
											final IAEItemStack out = realForFake == Actionable.SIMULATE ? null : Platform.poweredInsert( energy, storage, in, cct.getActionSource() );
											if( out != null )
											{
												craftMatrix.setInventorySlotContents( x, out.getItemStack() );
											}
											else
											{
												craftMatrix.setInventorySlotContents( x, null );
											}

											currentItem = craftMatrix.getStackInSlot( x );
										}
									}
								}

								// True if we need to fetch an item for the recipe
								if( patternItem != null && currentItem == null )
								{
									// Grab from network by recipe
									ItemStack whichItem = Platform.extractItemsByRecipe( energy, cct.getActionSource(), storage, player.worldObj, r, is, testInv, patternItem, x, all, realForFake, filter );

									// If that doesn't get it, grab exact items from network (?)
									// TODO see if this code is necessary
									if( whichItem == null )
									{
										for( int y = 0; y < this.recipe[x].length; y++ )
										{
											final IAEItemStack request = AEItemStack.create( this.recipe[x][y] );
											if( request != null )
											{
												if( filter == null || filter.isListed( request ) )
												{
													request.setStackSize( 1 );
													final IAEItemStack out = Platform.poweredExtraction( energy, storage, request, cct.getActionSource() );
													if( out != null )
													{
														whichItem = out.getItemStack();
														break;
													}
												}
											}
										}
									}

									// If that doesn't work, grab from the player's inventory
									if( whichItem == null && playerInventory != null )
									{
										whichItem = this.extractItemFromPlayerInventory( player, realForFake, patternItem );
									}

									craftMatrix.setInventorySlotContents( x, whichItem );
								}
							}
							con.onCraftMatrixChanged( craftMatrix );
						}
					}
				}
			}
		}
	}

	/**
	 * Tries to extract an item from the player inventory. Does account for fuzzy items.
	 *
	 * @param player      the {@link EntityPlayer} to extract from
	 * @param mode        the {@link Actionable} to simulate or modulate the operation
	 * @param patternItem which {@link ItemStack} to extract
	 * @return null or a found {@link ItemStack}
	 */
	private ItemStack extractItemFromPlayerInventory( final EntityPlayer player, final Actionable mode, final ItemStack patternItem )
	{
		final InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
		final AEItemStack request = AEItemStack.create( patternItem );
		final boolean isSimulated = mode == Actionable.SIMULATE;
		final boolean checkFuzzy = request.isOre() || patternItem.getItemDamage() == OreDictionary.WILDCARD_VALUE || patternItem.hasTagCompound() || patternItem.isItemStackDamageable();

		if( !checkFuzzy )
		{
			if( isSimulated )
			{
				return ia.simulateRemove( 1, patternItem, null );
			}
			else
			{
				return ia.removeItems( 1, patternItem, null );
			}
		}
		else
		{
			if( isSimulated )
			{
				return ia.simulateSimilarRemove( 1, patternItem, FuzzyMode.IGNORE_ALL, null );
			}
			else
			{
				return ia.removeSimilarItems( 1, patternItem, FuzzyMode.IGNORE_ALL, null );
			}
		}
	}
}
