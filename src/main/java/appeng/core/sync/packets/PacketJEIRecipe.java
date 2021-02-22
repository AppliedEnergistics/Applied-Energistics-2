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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IItemList;
import appeng.container.implementations.ContainerPatternTerm;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;


public class PacketJEIRecipe extends AppEngPacket
{

	private ItemStack[][] recipe;
	private ItemStack[] output;


	// automatic.
	public PacketJEIRecipe( final ByteBuf stream ) throws IOException
	{
		final ByteArrayInputStream bytes = this.getPacketByteArray( stream );
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
						this.recipe[x][y] = new ItemStack( list.getCompoundTagAt( y ) );
					}
				}
			}
			if ( comp.hasKey("outputs") ) {
				final NBTTagList outputList = comp.getTagList("outputs", 10);
				this.output = new ItemStack[3];
				for( int x = 0; x < this.output.length; x++ )
				{
					if( outputList.tagCount() > 0 )
					{
						this.output[x] = new ItemStack(outputList.getCompoundTagAt( x ));
					}
				}
			}
		}

	}

	// api
	public PacketJEIRecipe( final NBTTagCompound recipe ) throws IOException
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

		if( !( con instanceof IContainerCraftingPacket ) )
		{
			return;
		}

		final IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
		final IGridNode node = cct.getNetworkNode();

		if( node == null )
		{
			return;
		}

		final IGrid grid = node.getGrid();
		if( grid == null )
		{
			return;
		}

		final IStorageGrid inv = grid.getCache( IStorageGrid.class );
		final IEnergyGrid energy = grid.getCache( IEnergyGrid.class );
		final ISecurityGrid security = grid.getCache( ISecurityGrid.class );
		final ICraftingGrid crafting = grid.getCache( ICraftingGrid.class );
		final IItemHandler craftMatrix = cct.getInventoryByName( "crafting" );
		final IItemHandler playerInventory = cct.getInventoryByName( "player" );

		if( inv != null && this.recipe != null && security != null )
		{
			final IMEMonitor<IAEItemStack> storage = inv.getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
			final IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter( cct.getViewCells() );

			for( int x = 0; x < craftMatrix.getSlots(); x++ )
			{
				ItemStack currentItem = craftMatrix.getStackInSlot( x );

				// prepare slots
				if( !currentItem.isEmpty() )
				{
					// already the correct item?
					ItemStack newItem = this.canUseInSlot( x, currentItem );

					// put away old item
					if( newItem != currentItem && security.hasPermission( player, SecurityPermissions.INJECT ) )
					{
						final IAEItemStack in = AEItemStack.fromItemStack( currentItem );
						final IAEItemStack out = cct.useRealItems() ? Platform.poweredInsert( energy, storage, in, cct.getActionSource() ) : null;
						if( out != null )
						{
							currentItem = out.createItemStack();
						}
						else
						{
							currentItem = ItemStack.EMPTY;
						}
					}
				}

				if( currentItem.isEmpty() && this.recipe[x] != null )
				{
					// for each variant
					for( int y = 0; y < this.recipe[x].length && currentItem.isEmpty(); y++ )
					{
						final IAEItemStack request = AEItemStack.fromItemStack( this.recipe[x][y] );
						if( request != null )
						{
							// try ae
							if( ( filter == null || filter.isListed( request ) ) && security.hasPermission( player, SecurityPermissions.EXTRACT ) )
							{
								request.setStackSize( 1 );
								IAEItemStack out;

								if( cct.useRealItems() )
								{
									out = Platform.poweredExtraction( energy, storage, request, cct.getActionSource() );
									if( out == null )
									{
										Collection<IAEItemStack> outList = inv.getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ).getStorageList().findFuzzy( request, FuzzyMode.IGNORE_ALL );
										IAEItemStack mostDamaged = null;
										for( IAEItemStack is : outList )
										{
											if( !is.isCraftable() )
											{
												if( mostDamaged == null || mostDamaged.getItemDamage() < is.getItemDamage() )
												{
													mostDamaged = is;
												}
											}
										}
										if( mostDamaged != null )
										{
											mostDamaged.setStackSize( 1 );
											out = Platform.poweredExtraction( energy, storage, mostDamaged, cct.getActionSource() );
										}
									}
								}
								else
								{
									// Query the crafting grid if there is a pattern providing the item
									if( !crafting.getCraftingFor( request, null, 0, null ).isEmpty() )
									{
										out = request;
									}
									else
									{
										// Fall back using an existing item
										out = storage.extractItems( request, Actionable.SIMULATE, cct.getActionSource() );
									}

									if( out == null )
									{
										IItemList<IAEItemStack> items = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
										Iterator<IAEItemStack> iterator = inv.getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ).getAvailableItems( items ).iterator();
										while ( iterator.hasNext() )
										{
											final IAEItemStack o = iterator.next();
											if( o.getDefinition().isItemEqual( request.getDefinition() ) && o.isCraftable() )
											{
												out = o;
												break;
											}
											iterator.remove();
										}
									}
								}

								if( out != null )
								{
									out.setStackSize( recipe[x][y].getCount() );
									currentItem = out.createItemStack();
								}
							}

							// try inventory
							if( currentItem.isEmpty() )
							{
								AdaptorItemHandler ad = new AdaptorItemHandler( playerInventory );

								if( cct.useRealItems() )
								{
									currentItem = ad.removeSimilarItems(1, this.recipe[x][y],FuzzyMode.IGNORE_ALL, null );
								}
								else
								{
									currentItem = ad.simulateSimilarRemove(recipe[x][y].getCount(), this.recipe[x][y],FuzzyMode.IGNORE_ALL, null );
									if( currentItem.isEmpty() )
									{
										currentItem = this.recipe[x][y].copy();
									}
								}
							}
						}
					}
				}
				ItemHandlerUtil.setStackInSlot( craftMatrix, x, currentItem );
			}

			con.onCraftMatrixChanged( new WrapperInvItemHandler( craftMatrix ) );

			if( this.output != null && con instanceof ContainerPatternTerm && !( (ContainerPatternTerm) con ).isCraftingMode() )
			{
				IItemHandler outputSlots = cct.getInventoryByName( "output" );
				for( int i = 0; i < this.output.length; ++i )
				{
					ItemHandlerUtil.setStackInSlot( outputSlots, i, this.output[i] );
				}
			}
		}
	}

	/**
	 * 
	 * @param slot
	 * @param is itemstack
	 * @return is if it can be used, else EMPTY
	 */
	private ItemStack canUseInSlot( int slot, ItemStack is )
	{
		if( this.recipe[slot] != null )
		{
			for( ItemStack option : this.recipe[slot] )
			{
				if( is.isItemEqual( option ) )
				{
					return is;
				}
			}
		}
		return ItemStack.EMPTY;
	}

}
