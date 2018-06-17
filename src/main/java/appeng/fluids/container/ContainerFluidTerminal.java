/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.container;


import java.io.IOException;
import java.nio.BufferOverflowException;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEFluidInventoryUpdate;
import appeng.core.sync.packets.PacketTargetFluidStack;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.fluids.parts.PartFluidTerminal;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;


/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class ContainerFluidTerminal extends AEBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEFluidStack>
{
	private final IConfigManager clientCM;
	private final IMEMonitor<IAEFluidStack> monitor;
	private final IItemList<IAEFluidStack> fluids = AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ).createList();
	@GuiSync( 99 )
	public boolean hasPower = false;
	private PartFluidTerminal terminal;
	private IConfigManager serverCM;
	private IConfigManagerHost gui;
	private IGridNode networkNode;
	// Holds the fluid the client wishes to extract, or null for insert
	private IAEFluidStack clientRequestedTargetFluid = null;

	public ContainerFluidTerminal( InventoryPlayer ip, PartFluidTerminal terminal )
	{
		super( ip, terminal );
		this.terminal = terminal;
		this.clientCM = new ConfigManager( this );

		this.clientCM.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.clientCM.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
		this.clientCM.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		if( Platform.isServer() )
		{
			this.serverCM = terminal.getConfigManager();
			this.monitor = terminal.getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
			if( this.monitor != null )
			{
				this.monitor.addListener( this, null );
				final IGridNode node = terminal.getGridNode( AEPartLocation.INTERNAL );
				if( node != null )
				{
					this.networkNode = node;
					final IGrid g = node.getGrid();
					if( g != null )
					{
						this.setPowerSource( new ChannelPowerSrc( this.networkNode, g.getCache( IEnergyGrid.class ) ) );
					}
				}
			}
		}
		else
		{
			this.monitor = null;
		}
		this.bindPlayerInventory( ip, 0, 222 - 82 );
	}

	@Override
	public boolean isValid( Object verificationToken )
	{
		return true;
	}

	@Override
	public void postChange( IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, IActionSource actionSource )
	{
		for( final IAEFluidStack is : change )
		{
			this.fluids.add( is );
		}
	}

	@Override
	public void onListUpdate()
	{
		for( final IContainerListener c : this.listeners )
		{
			this.queueInventory( c );
		}
	}

	@Override
	public void addListener( IContainerListener listener )
	{
		super.addListener( listener );

		this.queueInventory( listener );
	}

	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );
		if( this.monitor != null )
		{
			this.monitor.removeListener( this );
		}
	}

	private void queueInventory( final IContainerListener c )
	{
		if( Platform.isServer() && c instanceof EntityPlayer && this.monitor != null )
		{
			try
			{
				PacketMEFluidInventoryUpdate piu = new PacketMEFluidInventoryUpdate();
				final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();

				for( final IAEFluidStack send : monitorCache )
				{
					try
					{
						piu.appendFluid( send );
					}
					catch( final BufferOverflowException boe )
					{
						NetworkHandler.instance().sendTo( piu, (EntityPlayerMP) c );

						piu = new PacketMEFluidInventoryUpdate();
						piu.appendFluid( send );
					}
				}

				NetworkHandler.instance().sendTo( piu, (EntityPlayerMP) c );
			}
			catch( final IOException e )
			{
				AELog.debug( e );
			}
		}
	}

	@Override
	public IConfigManager getConfigManager()
	{
		if( Platform.isServer() )
		{
			return this.serverCM;
		}
		return this.clientCM;
	}

	public void setTargetStack( final IAEFluidStack stack )
	{
		if( Platform.isClient() )
		{
			if( stack == null && this.clientRequestedTargetFluid == null )
			{
				return;
			}
			if( stack != null && this.clientRequestedTargetFluid != null && stack.getFluidStack()
					.isFluidEqual( this.clientRequestedTargetFluid.getFluidStack() ) )
			{
				return;
			}
			NetworkHandler.instance().sendToServer( new PacketTargetFluidStack( (AEFluidStack) stack ) );
		}

		this.clientRequestedTargetFluid = stack == null ? null : stack.copy();
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		if( this.getGui() != null )
		{
			this.getGui().updateSetting( manager, settingName, newValue );
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isServer() )
		{
			if( this.monitor != this.terminal.getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) ) )
			{
				this.setValidContainer( false );
			}

			for( final Settings set : this.serverCM.getSettings() )
			{
				final Enum<?> sideLocal = this.serverCM.getSetting( set );
				final Enum<?> sideRemote = this.clientCM.getSetting( set );

				if( sideLocal != sideRemote )
				{
					this.clientCM.putSetting( set, sideLocal );
					for( final IContainerListener crafter : this.listeners )
					{
						if( crafter instanceof EntityPlayerMP )
						{
							try
							{
								NetworkHandler.instance().sendTo( new PacketValueConfig( set.name(), sideLocal.name() ), (EntityPlayerMP) crafter );
							}
							catch( final IOException e )
							{
								AELog.debug( e );
							}
						}
					}
				}
			}

			if( !this.fluids.isEmpty() )
			{
				try
				{
					final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();

					final PacketMEFluidInventoryUpdate piu = new PacketMEFluidInventoryUpdate();

					for( final IAEFluidStack is : this.fluids )
					{
						final IAEFluidStack send = monitorCache.findPrecise( is );
						if( send == null )
						{
							is.setStackSize( 0 );
							piu.appendFluid( is );
						}
						else
						{
							piu.appendFluid( send );
						}
					}

					if( !piu.isEmpty() )
					{
						this.fluids.resetStatus();

						for( final Object c : this.listeners )
						{
							if( c instanceof EntityPlayer )
							{
								NetworkHandler.instance().sendTo( piu, (EntityPlayerMP) c );
							}
						}
					}
				}
				catch( final IOException e )
				{
					AELog.debug( e );
				}
			}
			this.updatePowerStatus();

			super.detectAndSendChanges();
		}
	}

	@Override
	public void doAction( EntityPlayerMP player, InventoryAction action, int slot, long id )
	{
		if( action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM )
		{
			super.doAction( player, action, slot, id );
			return;
		}
		ItemStack held = player.inventory.getItemStack();
		if( !held.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) )
		{ // For now only do simple i/o with held tanks
			return;
		}
		IFluidHandlerItem fh = FluidUtil.getFluidHandler( held );
		if( fh == null )
		{
			throw new NullPointerException( held.getDisplayName() + " did not give FLUID_HANDLER_ITEM_CAPABILITY" );
		}
		boolean isBucket = held.getItem() == Items.BUCKET ||
				held.getItem() == Items.WATER_BUCKET ||
				held.getItem() == Items.LAVA_BUCKET ||
				held.getItem() == Items.MILK_BUCKET ||
				held.getItem() == ForgeModContainer.getInstance().universalBucket;

		if( action == InventoryAction.FILL_ITEM && this.clientRequestedTargetFluid != null )
		{
			AEFluidStack stack = (AEFluidStack) this.clientRequestedTargetFluid.copy();

			AELog.info( "Filling %s with %s, %s mb", held.getDisplayName(), this.clientRequestedTargetFluid.getFluidStack().getLocalizedName(),
					stack.getStackSize() );

			if( isBucket && stack.getStackSize() < 1000 )
			{ // Although buckets support less than a buckets worth of fluid, it does not display how much it holds
				return;
			}

			// Check how much we can store in the item
			stack.setStackSize( Integer.MAX_VALUE );
			int amountAllowed = fh.fill( stack.getFluidStack(), false );
			stack.setStackSize( amountAllowed );

			// Check if we can pull out of the system
			IAEFluidStack canPull = this.monitor.extractItems( stack, Actionable.SIMULATE, this.getActionSource() );
			if( canPull == null || canPull.getStackSize() < 1 || ( isBucket && canPull.getStackSize() < 1000 ) )
			{
				// Either we couldn't pull out of the system,
				// or we are using a bucket and can only pull out less than a buckets worth of fluid
				return;
			}

			// Now actually pull out of the system
			IAEFluidStack pulled = Platform.poweredExtraction( this.getPowerSource(), this.monitor, stack, this.getActionSource() );
			if( pulled == null || pulled.getStackSize() < 1 )
			{
				// Something went wrong
				AELog.error( "Unable to pull fluid out of the ME system even though the simulation said yes " );
				return;
			}

			if( isBucket )
			{
				// We need to handle buckets separately
				ItemStack filledBucket = FluidUtil.getFilledBucket( pulled.getFluidStack() );
				player.inventory.setItemStack( filledBucket );
			}
			else
			{
				fh.fill( pulled.getFluidStack(), true );
			}
			this.updateHeld( player );
		}
		else if( action == InventoryAction.EMPTY_ITEM )
		{
			// Empty held item
			AELog.info( "Emptying %s", held.getDisplayName() );

			// See how much we can drain from the item
			FluidStack extract = fh.drain( Integer.MAX_VALUE, false );
			if( extract == null || extract.amount < 1 )
			{
				return;
			}

			// Check if we can push into the system
			IAEFluidStack canPush = this.monitor.injectItems( AEFluidStack.fromFluidStack( extract ), Actionable.SIMULATE, this.getActionSource() );
			if( isBucket && canPush != null && canPush.getStackSize() > 0 )
			{
				// We can't push enough for the bucket
				return;
			}

			IAEFluidStack inserted = Platform.poweredInsert( this.getPowerSource(), this.monitor, AEFluidStack.fromFluidStack( extract ),
					this.getActionSource() );
			if( inserted != null && inserted.getStackSize() > 0 )
			{
				// Only try to extract the amount we DID insert
				extract.amount -= Math.toIntExact( inserted.getStackSize() );
			}

			if( isBucket )
			{
				// Remove bucket and replace with EmptyBucket
				player.inventory.setItemStack( new ItemStack( Items.BUCKET, 1 ) );
			}
			else
			{
				// Actually drain
				fh.drain( extract, true );
			}
			this.updateHeld( player );
		}
	}

	protected void updatePowerStatus()
	{
		try
		{
			if( this.networkNode != null )
			{
				this.setPowered( this.networkNode.isActive() );
			}
			else if( this.getPowerSource() instanceof IEnergyGrid )
			{
				this.setPowered( ( (IEnergyGrid) this.getPowerSource() ).isNetworkPowered() );
			}
			else
			{
				this.setPowered( this.getPowerSource().extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.8 );
			}
		}
		catch( final Exception ignore )
		{
			// :P
		}
	}

	private IConfigManagerHost getGui()
	{
		return this.gui;
	}

	public void setGui( @Nonnull final IConfigManagerHost gui )
	{
		this.gui = gui;
	}

	public boolean isPowered()
	{
		return this.hasPower;
	}

	private void setPowered( final boolean isPowered )
	{
		this.hasPower = isPowered;
	}
}
