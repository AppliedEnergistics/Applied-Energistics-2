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

package appeng.parts.reporting;


import java.io.IOException;

import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.render.TesrRenderHelper;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.item.AEItemStack;


/**
 * A basic subclass for any item monitor like display with an item icon and an amount.
 *
 * It can also be used to extract items from somewhere and spawned into the world.
 *
 * @author AlgorithmX2
 * @author thatsIch
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartMonitor extends AbstractPartDisplay implements IPartStorageMonitor, IStackWatcherHost
{
	private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

	private IAEItemStack configuredItem;
	private IAEFluidStack configuredFluid;
	private String lastHumanReadableText;
	private boolean isLocked;
	private IStackWatcher myWatcher;

	@Reflected
	public AbstractPartMonitor( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );

		this.isLocked = data.getBoolean( "isLocked" );

		final NBTTagCompound myItem = data.getCompoundTag( "configuredItem" );
		this.configuredItem = AEItemStack.fromNBT( myItem );

		final NBTTagCompound myFluid = data.getCompoundTag( "configuredFluid" );
		this.configuredFluid = AEFluidStack.fromNBT( myFluid );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );

		data.setBoolean( "isLocked", this.isLocked );

		final NBTTagCompound myItem = new NBTTagCompound();
		if( this.configuredItem != null )
		{
			this.configuredItem.writeToNBT( myItem );
		}
		final NBTTagCompound myFluid = new NBTTagCompound();
		if( this.configuredFluid != null )
		{
			this.configuredFluid.writeToNBT( myFluid );
		}

		data.setTag( "configuredItem", myItem );
		data.setTag( "configuredFluid", myFluid );

	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );

		data.writeBoolean( this.isLocked );
		//is configured
		data.writeBoolean( this.configuredItem != null);
		data.writeBoolean( this.configuredFluid != null);
		if( this.configuredItem != null )
		{
			this.configuredItem.writeToPacket( data );
		}
		else if( this.configuredFluid != null )
		{
			this.configuredFluid.writeToPacket( data );
		}
	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		boolean needRedraw = super.readFromStream( data );

		final boolean isLocked = data.readBoolean();
		needRedraw = this.isLocked != isLocked;

		this.isLocked = isLocked;

		final boolean isItem = data.readBoolean();
		final boolean isFluid = data.readBoolean();
		if( isItem )
		{
			this.configuredItem = AEItemStack.fromPacket( data );
			this.configuredFluid = null;
		}
		else if( isFluid )
		{
			this.configuredFluid = AEFluidStack.fromPacket( data );
			this.configuredItem = null;
		}
		else
		{
			this.configuredItem = null;
			this.configuredFluid = null;
		}

		return needRedraw;
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( Platform.isClient() )
		{
			return true;
		}

		if( !this.getProxy().isActive() )
		{
			return false;
		}

		if( !Platform.hasPermissions( this.getLocation(), player ) )
		{
			return false;
		}

		if( !this.isLocked )
		{
			final ItemStack eq = player.getHeldItem( hand );
			FluidStack fluidInTank = null;

			if( eq.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) )
			{
				IFluidHandlerItem fluidHandlerItem = ( eq.getCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) );
				fluidInTank = fluidHandlerItem.drain( Integer.MAX_VALUE, false );
			}

			if (fluidInTank == null)
			{
				this.configuredFluid = null;
				this.configuredItem = AEItemStack.fromItemStack( eq );
			}
			else if( fluidInTank.amount > 0 )
			{
				this.configuredFluid = AEFluidStack.fromFluidStack( fluidInTank );
				this.configuredItem = null;
			}

			this.configureWatchers();
			this.getHost().markForSave();
			this.getHost().markForUpdate();
		}
		else
		{
			return super.onPartActivate( player, hand, pos );
		}

		return true;
	}

	@Override
	public boolean onPartShiftActivate( EntityPlayer player, EnumHand hand, Vec3d pos )
	{
		if( Platform.isClient() )
		{
			return true;
		}

		if( !this.getProxy().isActive() )
		{
			return false;
		}

		if( !Platform.hasPermissions( this.getLocation(), player ) )
		{
			return false;
		}

		if( player.getHeldItem( hand ).isEmpty() )
		{
			this.isLocked = !this.isLocked;
			player.sendMessage( ( this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked ).get() );
			this.getHost().markForSave();
			this.getHost().markForUpdate();
		}

		return true;
	}

	// update the system...
	private void configureWatchers()
	{
		if( this.myWatcher != null )
		{
			this.myWatcher.reset();
		}

		try
		{
			if( this.configuredItem != null )
			{
				if( this.myWatcher != null )
				{
					this.myWatcher.add( this.configuredItem );
				}

				this.updateReportingValue(
						this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ) );
			}
			else if ( this.configuredFluid != null )
			{
				if( this.myWatcher != null )
				{
					this.myWatcher.add( this.configuredFluid );
				}

				this.updateReportingValue(
						this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) ) );
			}
		}
		catch( final GridAccessException e )
		{
			// >.>
		}
	}

	private <T extends IAEStack<T>> void updateReportingValue ( final IMEMonitor<T> monitor )
	{

		if( this.configuredItem != null)
		{
			final IAEItemStack result = (IAEItemStack) monitor.getStorageList().findPrecise( (T) this.configuredItem );
			if( result == null )
			{
				this.configuredItem.setStackSize( 0 );
			}
			else
			{
				this.configuredItem.setStackSize( result.getStackSize() );
			}
		}
		else if( this.configuredFluid != null)
		{
			final IAEFluidStack result = (IAEFluidStack) monitor.getStorageList().findPrecise( (T) this.configuredFluid );
			if( result == null )
			{
				this.configuredFluid.setStackSize( 0 );
			}
			else
			{
				this.configuredFluid.setStackSize( result.getStackSize() );
			}
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderDynamic( double x, double y, double z, float partialTicks, int destroyStage )
	{

		if( ( this.getClientFlags() & ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) ) != ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) )
		{
			return;
		}

		IAEStack<?> ais = this.getDisplayed();

		if( ais == null )
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate( x + 0.5, y + 0.5, z + 0.5 );

		EnumFacing facing = this.getSide().getFacing();

		TesrRenderHelper.moveToFace( facing );
		TesrRenderHelper.rotateToFace( facing, this.getSpin() );
		if (ais instanceof IAEItemStack)
			TesrRenderHelper.renderItem2dWithAmount( (IAEItemStack) ais, 0.8f, 0.17f );
		if (ais instanceof IAEFluidStack)
			TesrRenderHelper.renderFluid2dWithAmount( (IAEFluidStack) ais, 0.8f, 0.17f );
		GlStateManager.popMatrix();

	}

	@Override
	public boolean requireDynamicRender()
	{
		return true;
	}

	@Override
	public IAEStack<?> getDisplayed()
	{
		if (this.configuredItem != null)
		return this.configuredItem;
		else if (this.configuredFluid != null)
			return this.configuredFluid;
		return null;
	}

	@Override
	public boolean isLocked()
	{
		return this.isLocked;
	}

	@Override
	public void updateWatcher( final IStackWatcher newWatcher )
	{
		this.myWatcher = newWatcher;
		this.configureWatchers();
	}

	@MENetworkEventSubscribe
	public void powerStatusChange( final MENetworkPowerStatusChange ev )
	{
		if( !this.getProxy().isPowered() )
		{
			if( this.myWatcher != null )
			{
				if( this.configuredItem != null )
				{
					this.configuredItem.setStackSize( 0 );
				}
				if( this.configuredFluid != null )
				{
					this.configuredFluid.setStackSize( 0 );
				}
			}
		}
		else
		{
			try
			{
				if( this.configuredItem != null )
				{
					this.updateReportingValue( this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ) );
				}
				if( this.configuredFluid != null )
				{
					this.updateReportingValue( this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) ) );
				}
			}
			catch( final GridAccessException e )
			{
				// ;P
			}
		}
	}

	@MENetworkEventSubscribe
	public void channelChanged( final MENetworkChannelsChanged c )
	{
		if( !this.getProxy().isPowered() )
		{
			if( this.myWatcher != null )
			{
				if( this.configuredItem != null )
				{
					this.configuredItem.setStackSize( 0 );
				}
				if( this.configuredFluid != null )
				{
					this.configuredFluid.setStackSize( 0 );
				}
			}
		}
		else
		{
			try
			{
				if( this.configuredItem != null )
				{
					this.updateReportingValue( this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ) );
				}
				if( this.configuredFluid != null )
				{
					this.updateReportingValue( this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) ) );
				}
			}
			catch( final GridAccessException e )
			{
				// ;P
			}
		}
	}

	@Override
	public void onStackChange( final IAEStack diffStack, final IStorageChannel chan )
	{
		if( this.configuredItem != null )
		{
			long diff = this.configuredItem.getStackSize() + diffStack.getStackSize();
			this.configuredItem.setStackSize( diff >= 0 ? this.configuredItem.getStackSize() + diffStack.getStackSize() : 0 );

			final long stackSize = this.configuredItem.getStackSize();
			final String humanReadableText = NUMBER_CONVERTER.toWideReadableForm( stackSize );

			if( !humanReadableText.equals( this.lastHumanReadableText ) )
			{
				this.lastHumanReadableText = humanReadableText;
				this.getHost().markForUpdate();
			}
		}
		else if( this.configuredFluid != null )
		{
			long diff = this.configuredFluid.getStackSize() + diffStack.getStackSize();
			this.configuredFluid.setStackSize( diff >= 0 ? this.configuredFluid.getStackSize() + diffStack.getStackSize() : 0 );

			final long stackSize = this.configuredFluid.getStackSize() / 1000;
			final String humanReadableText = NUMBER_CONVERTER.toWideReadableForm( stackSize ) + "B";

			if( !humanReadableText.equals( this.lastHumanReadableText ) )
			{
				this.lastHumanReadableText = humanReadableText;
				this.getHost().markForUpdate();
			}
		}
	}

	@Override
	public boolean showNetworkInfo( final RayTraceResult where )
	{
		return false;
	}

	protected IPartModel selectModel( IPartModel off, IPartModel on, IPartModel hasChannel, IPartModel lockedOff, IPartModel lockedOn, IPartModel lockedHasChannel )
	{
		if( this.isActive() )
		{
			if( this.isLocked() )
			{
				return lockedHasChannel;
			}
			else
			{
				return hasChannel;
			}
		}
		else if( this.isPowered() )
		{
			if( this.isLocked() )
			{
				return lockedOn;
			}
			else
			{
				return on;
			}
		}
		else
		{
			if( this.isLocked() )
			{
				return lockedOff;
			}
			else
			{
				return off;
			}
		}
	}

}
