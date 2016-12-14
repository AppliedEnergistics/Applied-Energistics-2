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

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
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
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );

		this.isLocked = data.getBoolean( "isLocked" );

		final NBTTagCompound myItem = data.getCompoundTag( "configuredItem" );
		this.configuredItem = AEItemStack.loadItemStackFromNBT( myItem );
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

		data.setTag( "configuredItem", myItem );
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
			this.configuredItem = AEItemStack.loadItemStackFromPacket( data );
		}
		else
		{
			this.configuredItem = null;
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

		final TileEntity te = this.getTile();
		final ItemStack eq = player.getHeldItem( hand );

		if( Platform.isWrench( player, eq, te.getPos() ) )
		{
			this.isLocked = !this.isLocked;
			player.addChatMessage( ( this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked ).get() );
			this.getHost().markForUpdate();
		}
		else if( !this.isLocked )
		{
			this.configuredItem = AEItemStack.create( eq );
			this.configureWatchers();
			this.getHost().markForUpdate();
		}
		else
		{
			this.extractItem( player );
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

				this.updateReportingValue( this.getProxy().getStorage().getItemInventory() );
			}
		}
		catch( final GridAccessException e )
		{
			// >.>
		}
	}

	protected void extractItem( final EntityPlayer player )
	{

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
	@SideOnly( Side.CLIENT )
	public void renderDynamic( double x, double y, double z, float partialTicks, int destroyStage )
	{

		if( ( this.getClientFlags() & ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) ) != ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) )
		{
			return;
		}

		final IAEItemStack ais = (IAEItemStack) this.getDisplayed();

		if( ais == null )
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate( x + 0.5, y + 0.5, z + 0.5 );

		EnumFacing facing = this.getSide().getFacing();

		TesrRenderHelper.moveToFace( facing );
		TesrRenderHelper.rotateToFace( facing, getSpin() );
		TesrRenderHelper.renderItem2dWithAmount( ais, 0.8f, 0.17f );

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
	public void onStackChange( final IItemList o, final IAEStack fullStack, final IAEStack diffStack, final BaseActionSource src, final StorageChannel chan )
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
		if( isActive() )
		{
			if( isLocked() )
			{
				return lockedHasChannel;
			}
			else
			{
				return hasChannel;
			}
		}
		else if( isPowered() )
		{
			if( isLocked() )
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
			if( isLocked() )
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
