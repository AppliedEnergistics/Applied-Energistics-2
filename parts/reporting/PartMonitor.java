package appeng.parts.reporting;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.parts.IPartMonitor;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartCollsionHelper;
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

	final int POWERED_FLAG = 1;
	final int BOOTING_FLAG = 2;
	final int CHANNEL_FLAG = 4;

	int clientFlags = 0; // sent as byte.

	@MENetworkEventSubscribe
	public void bootingRender(MENetworkBootingStatusChange c)
	{
		if ( notLightSource )
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
		clientFlags = 0;

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
		if ( clientFlags == oldFlags )
			return false;
		return true;
	}

	@Override
	public int getLightLevel()
	{
		return isPowered() ? (notLightSource ? 9 : 15) : 0;
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
		super( PartMonitor.class, is );
	}

	protected PartMonitor(Class c, ItemStack is) {
		super( c, is );
		if ( notLightSource )
			proxy.setFlags( GridFlags.REQUIRE_CHANNEL );
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
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		if ( getLightLevel() > 0 )
		{
			int l = 13;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
		}

		Tessellator.instance.setColorOpaque_I( getColor().whiteVariant );
		rh.renderFace( x, y, z, frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( getColor().mediumVariant );
		rh.renderFace( x, y, z, frontDark.getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( getColor().blackVariant );
		rh.renderFace( x, y, z, frontColored.getIcon(), ForgeDirection.SOUTH, renderer );

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
	public void getBoxes(IPartCollsionHelper bch)
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
