package appeng.parts.reporting;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.ENTITY;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.implementations.IPartStorageMonitor;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.texture.CableBusTextures;
import appeng.core.localization.PlayerMessages;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PartStorageMonitor extends PartMonitor implements IPartStorageMonitor, IStackWatcherHost
{

	byte spin;

	IAEItemStack configuredItem;
	boolean isLocked;

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
		byte rotation = (byte) (MathHelper.floor_double( (double) ((player.rotationYaw * 4F) / 360F) + 2.5D ) & 3);
		if ( side == ForgeDirection.UP )
			spin = rotation;
		else if ( side == ForgeDirection.DOWN )
			spin = rotation;
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		data.setByte( "spin", spin );
		data.setBoolean( "isLocked", isLocked );

		NBTTagCompound myItem = new NBTTagCompound();
		if ( configuredItem != null )
			configuredItem.writeToNBT( myItem );

		data.setCompoundTag( "configuredItem", myItem );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );

		spin = data.getByte( "spin" );
		isLocked = data.getBoolean( "isLocked" );

		NBTTagCompound myItem = data.getCompoundTag( "configuredItem" );
		configuredItem = AEItemStack.loadItemStackFromNBT( myItem );
	}

	@Override
	public void writeToStream(DataOutputStream data) throws IOException
	{
		super.writeToStream( data );

		data.writeByte( spin );
		data.writeBoolean( configuredItem != null ? true : false );
		if ( configuredItem != null )
			configuredItem.writeToPacket( data );
	}

	@Override
	public boolean readFromStream(DataInputStream data) throws IOException
	{
		boolean stuff = super.readFromStream( data );

		spin = data.readByte();
		boolean val = data.readBoolean();
		if ( val )
			configuredItem = AEItemStack.loadItemStackFromPacket( data );
		else
			configuredItem = null;

		return stuff;
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		if ( Platform.isClient() )
			return true;

		TileEntity te = this.tile;
		ItemStack eq = player.getCurrentEquippedItem();
		if ( Platform.isWrench( player, eq, te.xCoord, te.yCoord, te.zCoord ) )
		{
			isLocked = !isLocked;
			player.sendChatToPlayer( (isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).get() );
		}
		else if ( !isLocked )
		{
			configuredItem = AEItemStack.create( eq );
			confgiureWatchers();
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
		super( myClass, is );
	}

	public PartStorageMonitor(ItemStack is) {
		super( PartStorageMonitor.class, is );
		frontBright = CableBusTextures.PartStorageMonitor_Bright;
		frontColored = CableBusTextures.PartStorageMonitor_Colored;
		frontDark = CableBusTextures.PartStorageMonitor_Dark;
		frontSolid = CableBusTextures.PartStorageMonitor_Solid;
	}

	@Override
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator tess = Tessellator.instance;
		if ( tess.isDrawing )
			return;

		if ( (clientFlags & (POWERED_FLAG | CHANNEL_FLAG)) != (POWERED_FLAG | CHANNEL_FLAG) )
			return;

		IAEItemStack ais = (IAEItemStack) getDisplayed();
		if ( ais != null )
		{
			GL11.glPushMatrix();
			GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
			ForgeDirection d = side;
			GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );
			GL11.glTranslated( d.offsetX * 0.76, d.offsetY * 0.76, d.offsetZ * 0.76 );

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

				GL11.glTranslatef( 0.0f, -0.05f, -0.25f );
				GL11.glScalef( 1.0f / 1.5f, 1.0f / 1.5f, 1.0f / 1.5f );
				// GL11.glTranslated( -8.0, -12.2, -10.6 );
				GL11.glScalef( 1.0f, -1.0f, 0.005f );
				// GL11.glScalef( 1.0f , -1.0f, 1.0f );

				int k = sis.itemID;
				Block block = (k < Block.blocksList.length ? Block.blocksList[k] : null);
				if ( (sis.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d( Block.blocksList[k].getRenderType() )) )
				{
					GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
					GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
					GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				}

				int br = 16 << 20 | 16 << 4;
				int var11 = br % 65536;
				int var12 = br / 65536;
				OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL12.GL_RESCALE_NORMAL );
				// RenderHelper.enableGUIStandardItemLighting();
				tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

				doRenderItem( sis, this.tile );

			}
			catch (Exception err)
			{
				err.printStackTrace();
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
			GL11.glPopMatrix();
		}
	}

	private void doRenderItem(ItemStack itemstack, TileEntity par1EntityItemFrame)
	{
		if ( itemstack != null )
		{
			EntityItem entityitem = new EntityItem( par1EntityItemFrame.worldObj, 0.0D, 0.0D, 0.0D, itemstack );
			entityitem.getEntityItem().stackSize = 1;

			// set all this stuff and then do shit? meh?
			entityitem.hoverStart = 0;
			entityitem.age = 0;
			entityitem.rotationYaw = 0;

			GL11.glPushMatrix();
			GL11.glTranslatef( 0, -0.04F, 0 );
			// GL11.glDisable( GL11.GL_CULL_FACE );

			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer( itemstack, ENTITY );
			if ( customRenderer != null && !(itemstack.getItem() instanceof ItemBlock) )
			{
				if ( customRenderer.shouldUseRenderHelper( ENTITY, itemstack, BLOCK_3D ) )
				{
					GL11.glTranslatef( 0, -0.04F, 0 );
					GL11.glScalef( 0.7f, 0.7f, 0.7f );
					GL11.glRotatef( 35, 1, 0, 0 );
					GL11.glRotatef( 45, 0, 1, 0 );
					GL11.glRotatef( -90, 0, 1, 0 );
				}
			}
			else if ( itemstack.getItem() instanceof ItemBlock )
			{
				GL11.glTranslatef( 0, -0.04F, 0 );
				GL11.glScalef( 1.1f, 1.1f, 1.1f );
				GL11.glRotatef( -90, 0, 1, 0 );
			}
			else
			{
				GL11.glTranslatef( 0, -0.14F, 0 );
				GL11.glScalef( 0.8f, 0.8f, 0.8f );
			}

			RenderItem.renderInFrame = false;
			RenderManager.instance.renderEntityWithPosYaw( entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F );
			RenderItem.renderInFrame = false;

			GL11.glPopMatrix();
		}
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
		confgiureWatchers();
	}

	// update the system...
	public void confgiureWatchers()
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

}
