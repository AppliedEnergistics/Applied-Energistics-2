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

package appeng.parts.misc;


import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.Reflected;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;


public class PartInterface extends PartBasicState implements IGridTickable, IStorageMonitorable, IInventoryDestination, IInterfaceHost, ISidedInventory, IAEAppEngInventory, ITileStorageMonitorable, IPriorityHost
{

	private final DualityInterface duality = new DualityInterface( this.getProxy(), this );

	@Reflected
	public PartInterface( final ItemStack is )
	{
		super( is );
	}

	@MENetworkEventSubscribe
	public void stateChange( final MENetworkChannelsChanged c )
	{
		this.duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange( final MENetworkPowerStatusChange c )
	{
		this.duality.notifyNeighbors();
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.duality.getInstalledUpgrades( u );
	}

	@Override
	public void gridChanged()
	{
		this.duality.gridChanged();
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.duality.readFromNBT( data );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.duality.writeToNBT( data );
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.duality.initialize();
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		this.duality.addDrops( drops );
	}

	@Override
	public int getCableConnectionLength()
	{
		return 4;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.duality.getConfigManager();
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		return this.duality.getInventoryByName( name );
	}

	@Override
	public boolean onPartActivate( final EntityPlayer p, final EnumHand hand, final Vec3d pos )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		if( Platform.isServer() )
		{
			Platform.openGUI( p, this.getTileEntity(), this.getSide(), GuiBridge.GUI_INTERFACE );
		}

		return true;
	}

	@Override
	public boolean canInsert( final ItemStack stack )
	{
		return this.duality.canInsert( stack );
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.duality.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return this.duality.getFluidInventory();
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return this.duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		return this.duality.tickingRequest( node, ticksSinceLastCall );
	}

	@Override
	public int getSizeInventory()
	{
		return this.duality.getStorage().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		return this.duality.getStorage().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( final int i, final int j )
	{
		return this.duality.getStorage().decrStackSize( i, j );
	}

	@Override
	public ItemStack removeStackFromSlot( final int i )
	{
		return this.duality.getStorage().removeStackFromSlot( i );
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		this.duality.getStorage().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getName()
	{
		return this.duality.getStorage().getName();
	}

	@Override
	public boolean hasCustomName()
	{
		return this.duality.getStorage().hasCustomName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.duality.getStorage().getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		this.duality.getStorage().markDirty();
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer entityplayer )
	{
		return this.duality.getStorage().isUseableByPlayer( entityplayer );
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{
		this.duality.getStorage().openInventory( player );
	}

	@Override
	public void closeInventory( final EntityPlayer player )
	{
		this.duality.getStorage().closeInventory( player );
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return this.duality.getStorage().isItemValidForSlot( i, itemstack );
	}

	@Override
	public int[] getSlotsForFace( final EnumFacing side )
	{
		return this.duality.getSlotsForFace( side );
	}

	@Override
	public boolean canInsertItem( final int i, final ItemStack itemstack, final EnumFacing j )
	{
		return true;
	}

	@Override
	public boolean canExtractItem( final int i, final ItemStack itemstack, final EnumFacing j )
	{
		return true;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		this.duality.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public DualityInterface getInterfaceDuality()
	{
		return this.duality;
	}

	@Override
	public EnumSet<EnumFacing> getTargets()
	{
		return EnumSet.of( this.getSide().getFacing() );
	}

	@Override
	public TileEntity getTileEntity()
	{
		return super.getHost().getTile();
	}

	@Override
	public IStorageMonitorable getMonitorable( final EnumFacing side, final BaseActionSource src )
	{
		return this.duality.getMonitorable( side, src, this );
	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final InventoryCrafting table )
	{
		return this.duality.pushPattern( patternDetails, table );
	}

	@Override
	public boolean isBusy()
	{
		return this.duality.isBusy();
	}

	@Override
	public void provideCrafting( final ICraftingProviderHelper craftingTracker )
	{
		this.duality.provideCrafting( craftingTracker );
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems( final ICraftingLink link, final IAEItemStack items, final Actionable mode )
	{
		return this.duality.injectCraftedItems( link, items, mode );
	}

	@Override
	public void jobStateChange( final ICraftingLink link )
	{
		this.duality.jobStateChange( link );
	}

	@Override
	public int getPriority()
	{
		return this.duality.getPriority();
	}

	@Override
	public void setPriority( final int newValue )
	{
		this.duality.setPriority( newValue );
	}

	@Override
	public int getField( final int id )
	{
		return 0;
	}

	@Override
	public void setField( final int id, final int value )
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{

	}

	@Override
	public ITextComponent getDisplayName()
	{
		return null;
	}
}
