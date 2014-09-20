package appeng.parts.reporting;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IPartMonitor;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartMonitor extends AEBasePart implements IPartMonitor, IPowerChannelState
{

	// CableBusTextures frontSolid = CableBusTextures.PartMonitor_Solid;
	CableBusTextures frontDark = CableBusTextures.PartMonitor_Colored;
	CableBusTextures frontBright = CableBusTextures.PartMonitor_Bright;
	CableBusTextures frontColored = CableBusTextures.PartMonitor_Colored;

	boolean notLightSource = !this.getClass().equals( PartMonitor.class );

	final int POWERED_FLAG = 4;
	final int BOOTING_FLAG = 8;
	final int CHANNEL_FLAG = 16;

	byte spin = 0; // 0-3
	int clientFlags = 0; // sent as byte.
	float opacity = -1;

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
		super.onPlacement( player, held, side );

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
		data.setFloat( "opacity", opacity );
		data.setByte( "spin", (byte) spin );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		if ( data.hasKey( "opacity" ) )
			opacity = data.getFloat( "opacity" );
		spin = data.getByte( "spin" );
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		TileEntity te = getTile();

		if ( !player.isSneaking() && Platform.isWrench( player, player.inventory.getCurrentItem(), te.xCoord, te.yCoord, te.zCoord ) )
		{
			if ( Platform.isServer() )
			{
				if ( spin > 3 )
					spin = 0;

				switch (spin)
				{
				case 0:
					spin = 1;
					break;
				case 1:
					spin = 3;
					break;
				case 2:
					spin = 0;
					break;
				case 3:
					spin = 2;
					break;
				}

				this.host.markForUpdate();
				this.saveChanges();
			}
			return true;
		}
		else
			return super.onPartActivate( player, pos );
	}

	@MENetworkEventSubscribe
	public void bootingRender(MENetworkBootingStatusChange c)
	{
		if ( notLightSource )
			getHost().markForUpdate();
	}

	@Override
	public void onNeighborChanged()
	{
		opacity = -1;
		getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		getHost().markForUpdate();
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{
		super.writeToStream( data );
		clientFlags = spin & 3;
		;

		try
		{
			if ( proxy.getEnergy().isNetworkPowered() )
				clientFlags = clientFlags | POWERED_FLAG;

			if ( proxy.getPath().isNetworkBooting() )
				clientFlags = clientFlags | BOOTING_FLAG;

			if ( proxy.getNode().meetsChannelRequirements() )
				clientFlags = clientFlags | CHANNEL_FLAG;
		}
		catch (GridAccessException e)
		{
			// um.. nothing.
		}

		data.writeByte( (byte) clientFlags );
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		super.readFromStream( data );
		int oldFlags = clientFlags;
		clientFlags = data.readByte();
		spin = (byte) (clientFlags & 3);
		if ( clientFlags == oldFlags )
			return false;
		return true;
	}

	@Override
	public int getLightLevel()
	{
		return blockLight( isPowered() ? (notLightSource ? 9 : 15) : 0 );
	}

	private int blockLight(int emit)
	{
		if ( opacity < 0 )
		{
			TileEntity te = this.getTile();
			opacity = 255 - te.getWorldObj().getBlockLightOpacity( te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ );
		}

		return (int) (emit * (opacity / 255.0f));
	}

	@Override
	public boolean isPowered()
	{
		try
		{
			if ( Platform.isServer() )
				return proxy.getEnergy().isNetworkPowered();
			else
				return ((clientFlags & POWERED_FLAG) == POWERED_FLAG);
		}
		catch (GridAccessException e)
		{
			return false;
		}
	}

	public PartMonitor(ItemStack is) {
		this( PartMonitor.class, is, false );
	}

	protected PartMonitor(Class c, ItemStack is, boolean requireChannel) {
		super( c, is );

		if ( requireChannel )
		{
			proxy.setFlags( GridFlags.REQUIRE_CHANNEL );
			proxy.setIdlePowerUsage( 1.0 / 2.0 );
		}
		else
			proxy.setIdlePowerUsage( 1.0 / 16.0 ); // lights drain a little bit.

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );
		rh.renderInventoryBox( renderer );

		rh.setInvColor( getColor().whiteVariant );
		rh.renderInventoryFace( frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setInvColor( getColor().mediumVariant );
		rh.renderInventoryFace( frontDark.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setInvColor( getColor().blackVariant );
		rh.renderInventoryFace( frontColored.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimplifiedRendering( x, y, z, this, renderCache );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		if ( getLightLevel() > 0 )
		{
			int l = 13;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this.spin;

		Tessellator.instance.setColorOpaque_I( getColor().whiteVariant );
		rh.renderFace( x, y, z, frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( getColor().mediumVariant );
		rh.renderFace( x, y, z, frontDark.getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( getColor().blackVariant );
		rh.renderFace( x, y, z, frontColored.getIcon(), ForgeDirection.SOUTH, renderer );

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

		if ( notLightSource )
		{
			rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
					CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
					CableBusTextures.PartMonitorSidesStatus.getIcon() );
		}

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		if ( notLightSource )
		{
			boolean hasChan = (clientFlags & (POWERED_FLAG | CHANNEL_FLAG)) == (POWERED_FLAG | CHANNEL_FLAG);
			boolean hasPower = (clientFlags & POWERED_FLAG) == POWERED_FLAG;

			if ( hasChan )
			{
				int l = 14;
				Tessellator.instance.setBrightness( l << 20 | l << 4 );
				Tessellator.instance.setColorOpaque_I( getColor().blackVariant );
			}
			else if ( hasPower )
			{
				int l = 9;
				Tessellator.instance.setBrightness( l << 20 | l << 4 );
				Tessellator.instance.setColorOpaque_I( getColor().whiteVariant );
			}
			else
			{
				Tessellator.instance.setBrightness( 0 );
				Tessellator.instance.setColorOpaque_I( 0x000000 );
			}

			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.EAST, renderer );
			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.WEST, renderer );
			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.UP, renderer );
			rh.renderFace( x, y, z, CableBusTextures.PartMonitorSidesStatusLights.getIcon(), ForgeDirection.DOWN, renderer );
		}

	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
		bch.addBox( 4, 4, 13, 12, 12, 14 );
	}

	@Override
	public boolean isActive()
	{
		if ( notLightSource )
			return ((clientFlags & (CHANNEL_FLAG | POWERED_FLAG)) == (CHANNEL_FLAG | POWERED_FLAG));
		else
			return isPowered();
	}

}
