package appeng.parts.reporting;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

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
import appeng.client.texture.CableBusTextures;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartStorageMonitor extends PartMonitor implements IPartStorageMonitor, IStackWatcherHost
{

	IAEItemStack configuredItem;
	boolean isLocked;

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		data.setBoolean( "isLocked", isLocked );

		NBTTagCompound myItem = new NBTTagCompound();
		if ( configuredItem != null )
			configuredItem.writeToNBT( myItem );

		data.setTag( "configuredItem", myItem );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );

		isLocked = data.getBoolean( "isLocked" );

		NBTTagCompound myItem = data.getCompoundTag( "configuredItem" );
		configuredItem = AEItemStack.loadItemStackFromNBT( myItem );
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{
		super.writeToStream( data );

		data.writeByte( spin );
		data.writeBoolean( isLocked );
		data.writeBoolean( configuredItem != null ? true : false );
		if ( configuredItem != null )
			configuredItem.writeToPacket( data );
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		boolean stuff = super.readFromStream( data );

		spin = data.readByte();
		isLocked = data.readBoolean();
		boolean val = data.readBoolean();
		if ( val )
			configuredItem = AEItemStack.loadItemStackFromPacket( data );
		else
			configuredItem = null;

		updateList = true;

		return stuff;
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( Platform.isClient() )
			return true;

		if ( !proxy.isActive() )
			return false;

		if ( !Platform.hasPermissions( getLocation(), player ) )
			return false;

		TileEntity te = this.tile;
		ItemStack eq = player.getCurrentEquippedItem();
		if ( Platform.isWrench( player, eq, te.xCoord, te.yCoord, te.zCoord ) )
		{
			isLocked = !isLocked;
			player.addChatMessage( (isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).get() );
			this.getHost().markForUpdate();
		}
		else if ( !isLocked )
		{
			configuredItem = AEItemStack.create( eq );
			configureWatchers();
			this.getHost().markForUpdate();
		}
		else
			extractItem( player );

		return true;
	}

	protected void extractItem(EntityPlayer player)
	{

	}

	protected PartStorageMonitor(Class myClass, ItemStack is) {
		super( myClass, is, true );
	}

	public PartStorageMonitor(ItemStack is) {
		super( PartStorageMonitor.class, is, true );
		frontBright = CableBusTextures.PartStorageMonitor_Bright;
		frontColored = CableBusTextures.PartStorageMonitor_Colored;
		frontDark = CableBusTextures.PartStorageMonitor_Dark;
		// frontSolid = CableBusTextures.PartStorageMonitor_Solid;
	}

	@Override
	public boolean requireDynamicRender()
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	private boolean updateList;

	@SideOnly(Side.CLIENT)
	private Integer dspList;

	@Override
	@SideOnly(Side.CLIENT)
	protected void finalize() throws Throwable
	{
		super.finalize();
		if ( dspList != null )
			GLAllocation.deleteDisplayLists( dspList );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		if ( dspList == null )
			dspList = GLAllocation.generateDisplayLists( 1 );

		Tessellator tess = Tessellator.instance;
		if ( Platform.isDrawing( tess ) )
			return;

		if ( (clientFlags & (POWERED_FLAG | CHANNEL_FLAG)) != (POWERED_FLAG | CHANNEL_FLAG) )
			return;

		IAEItemStack ais = (IAEItemStack) getDisplayed();
		if ( ais != null )
		{
			GL11.glPushMatrix();
			GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

			if ( updateList )
			{
				updateList = false;
				GL11.glNewList( dspList, GL11.GL_COMPILE_AND_EXECUTE );
				tesrRenderScreen( tess, ais );
				GL11.glEndList();
			}
			else
				GL11.glCallList( dspList );

			GL11.glPopMatrix();
		}
	}

	private void tesrRenderScreen(Tessellator tess, IAEItemStack ais)
	{
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		ForgeDirection d = side;
		GL11.glTranslated( d.offsetX * 0.77, d.offsetY * 0.77, d.offsetZ * 0.77 );

		if ( d == ForgeDirection.UP )
		{
			GL11.glScalef( 1.0f, -1.0f, 1.0f );
			GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( (float) spin * 90.0F, 0, 0, 1 );
		}

		if ( d == ForgeDirection.DOWN )
		{
			GL11.glScalef( 1.0f, -1.0f, 1.0f );
			GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( (float) spin * -90.0F, 0, 0, 1 );
		}

		if ( d == ForgeDirection.EAST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
		}

		if ( d == ForgeDirection.WEST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
		}

		if ( d == ForgeDirection.NORTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
		}

		if ( d == ForgeDirection.SOUTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
		}

		GL11.glPushMatrix();
		try
		{
			ItemStack sis = ais.getItemStack();
			sis.stackSize = 1;

			int br = 16 << 20 | 16 << 4;
			int var11 = br % 65536;
			int var12 = br / 65536;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );
			// RenderHelper.enableGUIStandardItemLighting();
			tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

			ClientHelper.proxy.doRenderItem( sis, this.tile.getWorldObj() );

		}
		catch (Exception e)
		{
			AELog.error( e );
		}

		GL11.glPopMatrix();

		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		long qty = ais.getStackSize();
		if ( qty > 999999999999L )
			qty = 999999999999L;

		String msg = Long.toString( qty );
		if ( qty > 1000000000 )
			msg = Long.toString( qty / 1000000000 ) + "B";
		else if ( qty > 1000000 )
			msg = Long.toString( qty / 1000000 ) + "M";
		else if ( qty > 9999 )
			msg = Long.toString( qty / 1000 ) + "K";

		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int width = fr.getStringWidth( msg );
		GL11.glTranslatef( -0.5f * width, 0.0f, -1.0f );
		fr.drawString( msg, 0, 0, 0 );

		GL11.glPopAttrib();
	}

	@Override
	public IAEStack getDisplayed()
	{
		return configuredItem;
	}

	@Override
	public boolean isLocked()
	{
		return isLocked;
	}

	IStackWatcher myWatcher;

	@Override
	public void updateWatcher(IStackWatcher newWatcher)
	{
		myWatcher = newWatcher;
		configureWatchers();
	}

	// update the system...
	public void configureWatchers()
	{
		if ( myWatcher != null )
			myWatcher.clear();

		try
		{
			if ( configuredItem != null )
			{
				if ( myWatcher != null )
					myWatcher.add( configuredItem );

				updateReportingValue( proxy.getStorage().getItemInventory() );
			}
		}
		catch (GridAccessException e)
		{
			// >.>
		}
	}

	private void updateReportingValue(IMEMonitor<IAEItemStack> itemInventory)
	{
		if ( configuredItem != null )
		{
			IAEItemStack result = itemInventory.getStorageList().findPrecise( configuredItem );
			if ( result == null )
				configuredItem.setStackSize( 0 );
			else
				configuredItem.setStackSize( result.getStackSize() );
		}
	}

	@Override
	public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan)
	{
		if ( configuredItem != null )
		{
			if ( fullStack == null )
				configuredItem.setStackSize( 0 );
			else
				configuredItem.setStackSize( fullStack.getStackSize() );

			getHost().markForUpdate();
		}
	}

	@Override
	public boolean showNetworkInfo(MovingObjectPosition where)
	{
		return false;
	}

}
