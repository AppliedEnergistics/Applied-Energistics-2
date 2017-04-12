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


import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.ClientHelper;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;


/**
 * A basic subclass for any item monitor like display with an item icon and an amount.
 * <p>
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
	@SideOnly( Side.CLIENT )
	private boolean updateList;
	@SideOnly( Side.CLIENT )
	private Integer dspList;

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

		this.updateList = true;

		return needRedraw;
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final Vec3 pos )
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
		final ItemStack eq = player.getCurrentEquippedItem();

		if( Platform.isWrench( player, eq, te.xCoord, te.yCoord, te.zCoord ) )
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
			this.myWatcher.clear();
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
	protected void finalize() throws Throwable
	{
		super.finalize();

		if( this.dspList != null )
		{
			GLAllocation.deleteDisplayLists( this.dspList );
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderDynamic( final double x, final double y, final double z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		if( this.dspList == null )
		{
			this.dspList = GLAllocation.generateDisplayLists( 1 );
		}

		final Tessellator tess = Tessellator.instance;

		if( ( this.getClientFlags() & ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) ) != ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) )
		{
			return;
		}

		final IAEItemStack ais = (IAEItemStack) this.getDisplayed();

		if( ais != null )
		{
			GL11.glPushMatrix();
			GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

			if( this.updateList )
			{
				this.updateList = false;
				GL11.glNewList( this.dspList, GL11.GL_COMPILE_AND_EXECUTE );
				this.tesrRenderScreen( tess, ais );
				GL11.glEndList();
			}
			else
			{
				GL11.glCallList( this.dspList );
			}

			GL11.glPopMatrix();
		}
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

	private void tesrRenderScreen( final Tessellator tess, final IAEItemStack ais )
	{
		// GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		final ForgeDirection d = this.getSide();

		GL11.glTranslated( d.offsetX * 0.77, d.offsetY * 0.77, d.offsetZ * 0.77 );

		switch( d )
		{
			case UP:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
				GL11.glRotatef( this.getSpin() * 90.0F, 0, 0, 1 );
				break;
			case DOWN:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
				GL11.glRotatef( this.getSpin() * -90.0F, 0, 0, 1 );
				break;
			case EAST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
				break;
			case WEST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
				break;
			case NORTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				break;
			case SOUTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
				break;

			default:
				break;
		}

		try
		{
			final ItemStack sis = ais.getItemStack();
			sis.stackSize = 1;

			final int br = 16 << 20 | 16 << 4;
			final int var11 = br % 65536;
			final int var12 = br / 65536;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );
			// RenderHelper.enableGUIStandardItemLighting();
			tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

			ClientHelper.proxy.doRenderItem( sis, this.getTile().getWorldObj() );
		}
		catch( final Exception e )
		{
			AELog.debug( e );
		}
		finally
		{
			GL11.glEnable( GL11.GL_LIGHTING );
			GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		}

		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		final long stackSize = ais.getStackSize();
		final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm( stackSize );

		final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		final int width = fr.getStringWidth( renderedStackSize );
		GL11.glTranslatef( -0.5f * width, 0.0f, -1.0f );
		fr.drawString( renderedStackSize, 0, 0, 0 );

		// GL11.glPopAttrib();
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
	public boolean showNetworkInfo( final MovingObjectPosition where )
	{
		return false;
	}
}
