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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableSet;

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
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
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

	final DualityInterface duality = new DualityInterface( this.proxy, this );

	@Reflected
	public PartInterface( ItemStack is )
	{
		super( is );
	}

	@MENetworkEventSubscribe
	public void stateChange( MENetworkChannelsChanged c )
	{
		this.duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange( MENetworkPowerStatusChange c )
	{
		this.duality.notifyNeighbors();
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	public int getInstalledUpgrades( Upgrades u )
	{
		return this.duality.getInstalledUpgrades( u );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 12, 11, 11, 13 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void gridChanged()
	{
		this.duality.gridChanged();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 5, 5, 12, 11, 11, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.duality.readFromNBT( data );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
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
	public void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		this.duality.addDrops( drops );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 4;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.duality.getConfigManager();
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		return this.duality.getInventoryByName( name );
	}

	@Override
	public boolean onPartActivate( EntityPlayer p, Vec3 pos )
	{
		if( p.isSneaking() )
			return false;

		if( Platform.isServer() )
			Platform.openGUI( p, this.getTileEntity(), this.side, GuiBridge.GUI_INTERFACE );

		return true;
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return this.is.getIconIndex();
	}

	@Override
	public boolean canInsert( ItemStack stack )
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
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return this.duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		return this.duality.tickingRequest( node, TicksSinceLastCall );
	}

	@Override
	public int getSizeInventory()
	{
		return this.duality.getStorage().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( int i )
	{
		return this.duality.getStorage().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( int i, int j )
	{
		return this.duality.getStorage().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int i )
	{
		return this.duality.getStorage().getStackInSlotOnClosing( i );
	}

	@Override
	public void setInventorySlotContents( int i, ItemStack itemstack )
	{
		this.duality.getStorage().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInventoryName()
	{
		return this.duality.getStorage().getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.duality.getStorage().hasCustomInventoryName();
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
	public boolean isUseableByPlayer( EntityPlayer entityplayer )
	{
		return this.duality.getStorage().isUseableByPlayer( entityplayer );
	}

	@Override
	public void openInventory()
	{
		this.duality.getStorage().openInventory();
	}

	@Override
	public void closeInventory()
	{
		this.duality.getStorage().closeInventory();
	}

	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return this.duality.getStorage().isItemValidForSlot( i, itemstack );
	}

	@Override
	public int[] getAccessibleSlotsFromSide( int s )
	{
		return this.duality.getAccessibleSlotsFromSide( s );
	}

	@Override
	public boolean canInsertItem( int i, ItemStack itemstack, int j )
	{
		return true;
	}

	@Override
	public boolean canExtractItem( int i, ItemStack itemstack, int j )
	{
		return true;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		this.duality.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public DualityInterface getInterfaceDuality()
	{
		return this.duality;
	}

	@Override
	public EnumSet<ForgeDirection> getTargets()
	{
		return EnumSet.of( this.side );
	}

	@Override
	public TileEntity getTileEntity()
	{
		return super.getHost().getTile();
	}

	@Override
	public IStorageMonitorable getMonitorable( ForgeDirection side, BaseActionSource src )
	{
		return this.duality.getMonitorable( side, src, this );
	}

	@Override
	public boolean pushPattern( ICraftingPatternDetails patternDetails, InventoryCrafting table )
	{
		return this.duality.pushPattern( patternDetails, table );
	}

	@Override
	public boolean isBusy()
	{
		return this.duality.isBusy();
	}

	@Override
	public void provideCrafting( ICraftingProviderHelper craftingTracker )
	{
		this.duality.provideCrafting( craftingTracker );
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems( ICraftingLink link, IAEItemStack items, Actionable mode )
	{
		return this.duality.injectCraftedItems( link, items, mode );
	}

	@Override
	public void jobStateChange( ICraftingLink link )
	{
		this.duality.jobStateChange( link );
	}

	@Override
	public int getPriority()
	{
		return this.duality.getPriority();
	}

	@Override
	public void setPriority( int newValue )
	{
		this.duality.setPriority( newValue );
	}
}
