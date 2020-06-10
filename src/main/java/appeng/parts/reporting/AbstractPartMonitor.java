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

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import appeng.api.AEApi;
import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.render.TesrRenderHelper;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.item.AEItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


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
	private String lastHumanReadableText;
	private boolean isLocked;
	private IStackWatcher myWatcher;

	@Reflected
	public AbstractPartMonitor( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void readFromNBT( final CompoundNBT data )
	{
		super.readFromNBT( data );

		this.isLocked = data.getBoolean( "isLocked" );

		final CompoundNBT myItem = data.getCompound( "configuredItem" );
		this.configuredItem = AEItemStack.fromNBT( myItem );
	}

	@Override
	public void writeToNBT( final CompoundNBT data )
	{
		super.writeToNBT( data );

		data.putBoolean( "isLocked", this.isLocked );

		final CompoundNBT myItem = new CompoundNBT();
		if( this.configuredItem != null )
		{
			this.configuredItem.write( myItem );
		}

		data.put( "configuredItem", myItem );
	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );

		data.writeBoolean( this.isLocked );
		data.writeBoolean( this.configuredItem != null );
		if( this.configuredItem != null )
		{
			this.configuredItem.writeToPacket( data );
		}
	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		boolean needRedraw = super.readFromStream( data );

		final boolean isLocked = data.readBoolean();
		needRedraw = this.isLocked != isLocked;

		this.isLocked = isLocked;

		final boolean val = data.readBoolean();
		if( val )
		{
			this.configuredItem = AEItemStack.fromPacket( data );
		}
		else
		{
			this.configuredItem = null;
		}

		return needRedraw;
	}

	@Override
	public boolean onPartActivate( final PlayerEntity player, final Hand hand, final Vec3d pos )
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
			this.configuredItem = AEItemStack.fromItemStack( eq );
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
	public boolean onPartShiftActivate( PlayerEntity player, Hand hand, Vec3d pos )
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
		}
		catch( final GridAccessException e )
		{
			// >.>
		}
	}

	private void updateReportingValue( final IMEMonitor<IAEItemStack> itemInventory )
	{
		if( this.configuredItem != null )
		{
			final IAEItemStack result = itemInventory.getStorageList().findPrecise( this.configuredItem );
			if( result == null )
			{
				this.configuredItem.setStackSize( 0 );
			}
			else
			{
				this.configuredItem.setStackSize( result.getStackSize() );
			}
		}
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void renderDynamic( double x, double y, double z, float partialTicks, int destroyStage )
	{

		if( ( this.getClientFlags() & ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) ) != ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) )
		{
			return;
		}

		final IAEItemStack ais = this.getDisplayed();

		if( ais == null )
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translated( x + 0.5, y + 0.5, z + 0.5 );

		Direction facing = this.getSide().getFacing();

		TesrRenderHelper.moveToFace( facing );
		TesrRenderHelper.rotateToFace( facing, this.getSpin() );
		TesrRenderHelper.renderItem2dWithAmount( ais, 0.8f, 0.17f );

		GlStateManager.popMatrix();

	}

	@Override
	public boolean requireDynamicRender()
	{
		return true;
	}

	@Override
	public IAEItemStack getDisplayed()
	{
		return this.configuredItem;
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

	@Override
	public void onStackChange( final IItemList o, final IAEStack fullStack, final IAEStack diffStack, final IActionSource src, final IStorageChannel chan )
	{
		if( this.configuredItem != null )
		{
			if( fullStack == null )
			{
				this.configuredItem.setStackSize( 0 );
			}
			else
			{
				this.configuredItem.setStackSize( fullStack.getStackSize() );
			}

			final long stackSize = this.configuredItem.getStackSize();
			final String humanReadableText = NUMBER_CONVERTER.toWideReadableForm( stackSize );

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
