package appeng.parts.networking;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.IReadOnlyCollection;
import appeng.block.AEBaseBlock;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.FlipableIcon;
import appeng.client.texture.TaughtIcon;
import appeng.items.parts.ItemMultiPart;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartCable extends AEBasePart implements IPartCable
{

	int channelsOnSide[] = new int[] { 0, 0, 0, 0, 0, 0 };

	EnumSet<ForgeDirection> connections = EnumSet.noneOf( ForgeDirection.class );
	boolean powered = false;

	public PartCable(Class c, ItemStack is) {
		super( c, is );
		proxy.setFlags( GridFlags.PREFERED );
		proxy.setIdlePowerUsage( 0.0 );
		proxy.myColor = AEColor.values()[((ItemMultiPart) is.getItem()).varientOf( is.getItemDamage() )];
	}

	@Override
	public boolean isConnected(ForgeDirection side)
	{
		return connections.contains( side );
	}

	@Override
	public BusSupport supportsBuses()
	{
		return BusSupport.CABLE;
	}

	public IIcon getGlassTexture(AEColor c)
	{
		switch (c)
		{
		case Black:
			return CableBusTextures.MECable_Black.getIcon();
		case Blue:
			return CableBusTextures.MECable_Blue.getIcon();
		case Brown:
			return CableBusTextures.MECable_Brown.getIcon();
		case Cyan:
			return CableBusTextures.MECable_Cyan.getIcon();
		case Gray:
			return CableBusTextures.MECable_Grey.getIcon();
		case Green:
			return CableBusTextures.MECable_Green.getIcon();
		case LightBlue:
			return CableBusTextures.MECable_LightBlue.getIcon();
		case LightGray:
			return CableBusTextures.MECable_LightGrey.getIcon();
		case Lime:
			return CableBusTextures.MECable_Lime.getIcon();
		case Magenta:
			return CableBusTextures.MECable_Magenta.getIcon();
		case Orange:
			return CableBusTextures.MECable_Orange.getIcon();
		case Pink:
			return CableBusTextures.MECable_Pink.getIcon();
		case Purple:
			return CableBusTextures.MECable_Purple.getIcon();
		case Red:
			return CableBusTextures.MECable_Red.getIcon();
		case White:
			return CableBusTextures.MECable_White.getIcon();
		case Yellow:
			return CableBusTextures.MECable_Yellow.getIcon();
		default:
		}
		return AEApi.instance().parts().partCableGlass.item( AEColor.Transparent ).getIconIndex(
				AEApi.instance().parts().partCableGlass.stack( AEColor.Transparent, 1 ) );
	}

	public IIcon getTexture(AEColor c)
	{
		return getGlassTexture( c );
	}

	public IIcon getCoveredTexture(AEColor c)
	{
		switch (c)
		{
		case Black:
			return CableBusTextures.MECovered_Black.getIcon();
		case Blue:
			return CableBusTextures.MECovered_Blue.getIcon();
		case Brown:
			return CableBusTextures.MECovered_Brown.getIcon();
		case Cyan:
			return CableBusTextures.MECovered_Cyan.getIcon();
		case Gray:
			return CableBusTextures.MECovered_Gray.getIcon();
		case Green:
			return CableBusTextures.MECovered_Green.getIcon();
		case LightBlue:
			return CableBusTextures.MECovered_LightBlue.getIcon();
		case LightGray:
			return CableBusTextures.MECovered_LightGrey.getIcon();
		case Lime:
			return CableBusTextures.MECovered_Lime.getIcon();
		case Magenta:
			return CableBusTextures.MECovered_Magenta.getIcon();
		case Orange:
			return CableBusTextures.MECovered_Orange.getIcon();
		case Pink:
			return CableBusTextures.MECovered_Pink.getIcon();
		case Purple:
			return CableBusTextures.MECovered_Purple.getIcon();
		case Red:
			return CableBusTextures.MECovered_Red.getIcon();
		case White:
			return CableBusTextures.MECovered_White.getIcon();
		case Yellow:
			return CableBusTextures.MECovered_Yellow.getIcon();
		default:
		}
		return AEApi.instance().parts().partCableCovered.item( AEColor.Transparent ).getIconIndex(
				AEApi.instance().parts().partCableCovered.stack( AEColor.Transparent, 1 ) );
	}

	public IIcon getSmartTexture(AEColor c)
	{
		switch (c)
		{
		case Black:
			return CableBusTextures.MESmart_Black.getIcon();
		case Blue:
			return CableBusTextures.MESmart_Blue.getIcon();
		case Brown:
			return CableBusTextures.MESmart_Brown.getIcon();
		case Cyan:
			return CableBusTextures.MESmart_Cyan.getIcon();
		case Gray:
			return CableBusTextures.MESmart_Gray.getIcon();
		case Green:
			return CableBusTextures.MESmart_Green.getIcon();
		case LightBlue:
			return CableBusTextures.MESmart_LightBlue.getIcon();
		case LightGray:
			return CableBusTextures.MESmart_LightGrey.getIcon();
		case Lime:
			return CableBusTextures.MESmart_Lime.getIcon();
		case Magenta:
			return CableBusTextures.MESmart_Magenta.getIcon();
		case Orange:
			return CableBusTextures.MESmart_Orange.getIcon();
		case Pink:
			return CableBusTextures.MESmart_Pink.getIcon();
		case Purple:
			return CableBusTextures.MESmart_Purple.getIcon();
		case Red:
			return CableBusTextures.MESmart_Red.getIcon();
		case White:
			return CableBusTextures.MESmart_White.getIcon();
		case Yellow:
			return CableBusTextures.MESmart_Yellow.getIcon();
		default:
		}
		return AEApi.instance().parts().partCableCovered.item( AEColor.Transparent ).getIconIndex(
				AEApi.instance().parts().partCableSmart.stack( AEColor.Transparent, 1 ) );
	}

	@Override
	public AEColor getCableColor()
	{
		return proxy.myColor;
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.GLASS;
	}

	public AENetworkProxy getProxy()
	{
		return proxy;
	}

	public void markForUpdate()
	{
		getHost().markForUpdate();
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		if ( Platform.isServer() )
		{
			IGridNode node = getGridNode();
			int howMany = 0;

			if ( node != null )
			{
				for (IGridConnection gc : node.getConnections())
					howMany = Math.max( gc.getUsedChannels(), howMany );

				data.setByte( "usedChannels", (byte) howMany );
			}
		}

	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{
		int cs = 0;
		int sideOut = 0;

		IGridNode n = getGridNode();
		if ( n != null )
		{
			for (ForgeDirection thisSide : ForgeDirection.VALID_DIRECTIONS)
			{
				IPart part = getHost().getPart( thisSide );
				if ( part != null )
				{
					if ( part.getGridNode() != null )
					{
						IReadOnlyCollection<IGridConnection> set = part.getGridNode().getConnections();
						for (IGridConnection gc : set)
						{
							if ( proxy.getNode().hasFlag( GridFlags.DENSE_CAPACITY ) && gc.getOtherSide( proxy.getNode() ).hasFlag( GridFlags.DENSE_CAPACITY ) )
								sideOut |= (gc.getUsedChannels() / 4) << (4 * thisSide.ordinal());
							else
								sideOut |= (gc.getUsedChannels()) << (4 * thisSide.ordinal());
						}
					}
				}
			}

			for (IGridConnection gc : n.getConnections())
			{
				ForgeDirection side = gc.getDirection( n );
				if ( side != ForgeDirection.UNKNOWN )
				{
					boolean isTier2a = proxy.getNode().hasFlag( GridFlags.DENSE_CAPACITY );
					boolean isTier2b = gc.getOtherSide( proxy.getNode() ).hasFlag( GridFlags.DENSE_CAPACITY );

					if ( isTier2a && isTier2b )
						sideOut |= (gc.getUsedChannels() / 4) << (4 * side.ordinal());
					else
						sideOut |= gc.getUsedChannels() << (4 * side.ordinal());
					cs |= (1 << side.ordinal());
				}
			}
		}

		try
		{
			if ( proxy.getEnergy().isNetworkPowered() )
				cs |= (1 << ForgeDirection.UNKNOWN.ordinal());
		}
		catch (GridAccessException e)
		{
			// aww...
		}

		data.writeByte( (byte) cs );
		data.writeInt( sideOut );
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		int cs = data.readByte();
		int sideOut = data.readInt();

		EnumSet<ForgeDirection> myC = connections.clone();
		boolean wasPowered = powered;
		powered = false;
		boolean chchanged = false;

		for (ForgeDirection d : ForgeDirection.values())
		{
			if ( d != ForgeDirection.UNKNOWN )
			{
				int ch = (sideOut >> (d.ordinal() * 4)) & 0xF;
				if ( ch != channelsOnSide[d.ordinal()] )
				{
					chchanged = true;
					channelsOnSide[d.ordinal()] = ch;
				}
			}

			if ( d == ForgeDirection.UNKNOWN )
			{
				int id = 1 << d.ordinal();
				if ( id == (cs & id) )
					powered = true;
			}
			else
			{
				int id = 1 << d.ordinal();
				if ( id == (cs & id) )
					connections.add( d );
				else
					connections.remove( d );
			}
		}

		return !myC.equals( connections ) || wasPowered != powered || chchanged;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 6.0, 6.0, 6.0, 10.0, 10.0, 10.0 );

		if ( Platform.isServer() )
		{
			IGridNode n = getGridNode();
			if ( n != null )
				connections = n.getConnectedSides();
			else
				connections.clear();
		}

		IPartHost ph = getHost();
		if ( ph != null )
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				IPart p = ph.getPart( dir );
				if ( p instanceof IGridHost )
				{
					double dist = p.cableConnectionRenderTo();

					if ( dist > 8 )
						continue;

					switch (dir)
					{
					case DOWN:
						bch.addBox( 6.0, dist, 6.0, 10.0, 6.0, 10.0 );
						break;
					case EAST:
						bch.addBox( 10.0, 6.0, 6.0, 16.0 - dist, 10.0, 10.0 );
						break;
					case NORTH:
						bch.addBox( 6.0, 6.0, dist, 10.0, 10.0, 6.0 );
						break;
					case SOUTH:
						bch.addBox( 6.0, 6.0, 10.0, 10.0, 10.0, 16.0 - dist );
						break;
					case UP:
						bch.addBox( 6.0, 10.0, 6.0, 10.0, 16.0 - dist, 10.0 );
						break;
					case WEST:
						bch.addBox( dist, 6.0, 6.0, 6.0, 10.0, 10.0 );
						break;
					default:
						continue;
					}
				}
			}
		}

		for (ForgeDirection of : connections)
		{
			switch (of)
			{
			case DOWN:
				bch.addBox( 6.0, 0.0, 6.0, 10.0, 6.0, 10.0 );
				break;
			case EAST:
				bch.addBox( 10.0, 6.0, 6.0, 16.0, 10.0, 10.0 );
				break;
			case NORTH:
				bch.addBox( 6.0, 6.0, 0.0, 10.0, 10.0, 6.0 );
				break;
			case SOUTH:
				bch.addBox( 6.0, 6.0, 10.0, 10.0, 10.0, 16.0 );
				break;
			case UP:
				bch.addBox( 6.0, 10.0, 6.0, 10.0, 16.0, 10.0 );
				break;
			case WEST:
				bch.addBox( 0.0, 6.0, 6.0, 6.0, 10.0, 10.0 );
				break;
			default:
				continue;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );

		rh.setTexture( getTexture( getCableColor() ) );
		rh.setBounds( 6.0f, 6.0f, 2.0f, 10.0f, 10.0f, 14.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBreakingTexture()
	{
		return getTexture( getCableColor() );
	}

	@SideOnly(Side.CLIENT)
	public void rendereGlassConection(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer, ForgeDirection of)
	{
		TileEntity te = this.tile.getWorldObj().getTileEntity( x + of.offsetX, y + of.offsetY, z + of.offsetZ );
		IPartHost ccph = te instanceof IPartHost ? (IPartHost) te : null;
		IGridHost gh = te instanceof IGridHost ? (IGridHost) te : null;

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of ) ) );

		if ( gh != null && ccph != null && gh.getCableConnectionType( of ) == AECableType.GLASS && ccph.getColor() != AEColor.Transparent
				&& ccph.getPart( of.getOpposite() ) == null )
			rh.setTexture( getTexture( ccph.getColor() ) );
		else if ( ccph == null && gh != null && gh.getCableConnectionType( of ) != AECableType.GLASS )
		{
			rh.setTexture( getCoveredTexture( getCableColor() ) );
			switch (of)
			{
			case DOWN:
				rh.setBounds( 5, 0, 5, 11, 4, 11 );
				break;
			case EAST:
				rh.setBounds( 12, 5, 5, 16, 11, 11 );
				break;
			case NORTH:
				rh.setBounds( 5, 5, 0, 11, 11, 4 );
				break;
			case SOUTH:
				rh.setBounds( 5, 5, 12, 11, 11, 16 );
				break;
			case UP:
				rh.setBounds( 5, 12, 5, 11, 16, 11 );
				break;
			case WEST:
				rh.setBounds( 0, 5, 5, 4, 11, 11 );
				break;
			default:
				return;
			}

			rh.renderBlock( x, y, z, renderer );
			rh.setTexture( getTexture( getCableColor() ) );
		}
		else
			rh.setTexture( getTexture( getCableColor() ) );

		switch (of)
		{
		case DOWN:
			rh.setBounds( 6, 0, 6, 10, 6, 10 );
			break;
		case EAST:
			rh.setBounds( 10, 6, 6, 16, 10, 10 );
			break;
		case NORTH:
			rh.setBounds( 6, 6, 0, 10, 10, 6 );
			break;
		case SOUTH:
			rh.setBounds( 6, 6, 10, 10, 10, 16 );
			break;
		case UP:
			rh.setBounds( 6, 10, 6, 10, 16, 10 );
			break;
		case WEST:
			rh.setBounds( 0, 6, 6, 6, 10, 10 );
			break;
		default:
			return;
		}

		rh.renderBlock( x, y, z, renderer );
		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
	}

	protected CableBusTextures getChannelTex(int i, boolean b)
	{
		if ( !powered )
			i = 0;

		if ( b )
		{
			switch (i)
			{
			default:
				return CableBusTextures.Channels10;
			case 5:
				return CableBusTextures.Channels11;
			case 6:
				return CableBusTextures.Channels12;
			case 7:
				return CableBusTextures.Channels13;
			case 8:
				return CableBusTextures.Channels14;
			}
		}
		else
		{
			switch (i)
			{
			case 0:
				return CableBusTextures.Channels00;
			case 1:
				return CableBusTextures.Channels01;
			case 2:
				return CableBusTextures.Channels02;
			case 3:
				return CableBusTextures.Channels03;
			default:
				return CableBusTextures.Channels04;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void renderCoveredConection(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer, int channels, ForgeDirection of)
	{
		TileEntity te = this.tile.getWorldObj().getTileEntity( x + of.offsetX, y + of.offsetY, z + of.offsetZ );
		IPartHost ccph = te instanceof IPartHost ? (IPartHost) te : null;
		IGridHost ghh = te instanceof IGridHost ? (IGridHost) te : null;
		boolean isSmart = false;

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of.getOpposite() ) ) );
		if ( ghh != null && ccph != null && ghh.getCableConnectionType( of.getOpposite() ) == AECableType.GLASS && ccph.getPart( of.getOpposite() ) == null
				&& ccph.getColor() != AEColor.Transparent )
			rh.setTexture( getGlassTexture( ccph.getColor() ) );
		else if ( ccph == null && ghh != null && ghh.getCableConnectionType( of ) != AECableType.GLASS )
		{
			rh.setTexture( getCoveredTexture( getCableColor() ) );
			switch (of)
			{
			case DOWN:
				rh.setBounds( 5, 0, 5, 11, 4, 11 );
				break;
			case EAST:
				rh.setBounds( 12, 5, 5, 16, 11, 11 );
				break;
			case NORTH:
				rh.setBounds( 5, 5, 0, 11, 11, 4 );
				break;
			case SOUTH:
				rh.setBounds( 5, 5, 12, 11, 11, 16 );
				break;
			case UP:
				rh.setBounds( 5, 12, 5, 11, 16, 11 );
				break;
			case WEST:
				rh.setBounds( 0, 5, 5, 4, 11, 11 );
				break;
			default:
				return;
			}

			rh.renderBlock( x, y, z, renderer );

			rh.setTexture( getTexture( getCableColor() ) );
		}
		else if ( ghh != null && ccph != null && ghh.getCableConnectionType( of ) == AECableType.COVERED && ccph.getColor() != AEColor.Transparent )
			rh.setTexture( getCoveredTexture( ccph.getColor() ) );
		else if ( ghh != null && ccph != null && ghh.getCableConnectionType( of ) == AECableType.SMART )
		{
			isSmart = true;
			rh.setTexture( getSmartTexture( getCableColor() ) );
		}
		else
			rh.setTexture( getCoveredTexture( getCableColor() ) );

		switch (of)
		{
		case DOWN:
			rh.setBounds( 6, 0, 6, 10, 5, 10 );
			break;
		case EAST:
			rh.setBounds( 11, 6, 6, 16, 10, 10 );
			break;
		case NORTH:
			rh.setBounds( 6, 6, 0, 10, 10, 5 );
			break;
		case SOUTH:
			rh.setBounds( 6, 6, 11, 10, 10, 16 );
			break;
		case UP:
			rh.setBounds( 6, 11, 6, 10, 16, 10 );
			break;
		case WEST:
			rh.setBounds( 0, 6, 6, 5, 10, 10 );
			break;
		default:
			return;
		}

		rh.renderBlock( x, y, z, renderer );
		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );

		if ( isSmart )
		{
			setSmartConnectionRotations( of, renderer );
			IIcon defa = new TaughtIcon( getChannelTex( channels, false ).getIcon(), -0.2f );
			IIcon defb = new TaughtIcon( getChannelTex( channels, true ).getIcon(), -0.2f );

			if ( of == ForgeDirection.EAST || of == ForgeDirection.WEST )
			{
				AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
				FlipableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST );
				ico.setFlip( false, true );
			}

			Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
			Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
			rh.setTexture( defa, defa, defa, defa, defa, defa );
			renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
			rh.setTexture( defb, defb, defb, defb, defb, defb );
			renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		}

		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
	}

	@SideOnly(Side.CLIENT)
	public void renderSmartConection(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer, int channels, ForgeDirection of)
	{
		TileEntity te = this.tile.getWorldObj().getTileEntity( x + of.offsetX, y + of.offsetY, z + of.offsetZ );
		IPartHost ccph = te instanceof IPartHost ? (IPartHost) te : null;
		IGridHost ghh = te instanceof IGridHost ? (IGridHost) te : null;
		boolean isGlass = false;
		AEColor myColor = getCableColor();

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of, of.getOpposite() ) ) );

		if ( ghh != null && ccph != null && ghh.getCableConnectionType( of.getOpposite() ) == AECableType.GLASS && ccph.getPart( of.getOpposite() ) == null
				&& ccph.getColor() != AEColor.Transparent )
		{
			isGlass = true;
			rh.setTexture( getGlassTexture( myColor = ccph.getColor() ) );
		}
		else if ( ccph == null && ghh != null && ghh.getCableConnectionType( of.getOpposite() ) != AECableType.GLASS )
		{
			rh.setTexture( getSmartTexture( myColor ) );
			switch (of)
			{
			case DOWN:
				rh.setBounds( 5, 0, 5, 11, 4, 11 );
				break;
			case EAST:
				rh.setBounds( 12, 5, 5, 16, 11, 11 );
				break;
			case NORTH:
				rh.setBounds( 5, 5, 0, 11, 11, 4 );
				break;
			case SOUTH:
				rh.setBounds( 5, 5, 12, 11, 11, 16 );
				break;
			case UP:
				rh.setBounds( 5, 12, 5, 11, 16, 11 );
				break;
			case WEST:
				rh.setBounds( 0, 5, 5, 4, 11, 11 );
				break;
			default:
				return;
			}
			rh.renderBlock( x, y, z, renderer );

			if ( true )
			{
				setSmartConnectionRotations( of, renderer );
				IIcon defa = new TaughtIcon( getChannelTex( channels, false ).getIcon(), -0.2f );
				IIcon defb = new TaughtIcon( getChannelTex( channels, true ).getIcon(), -0.2f );

				if ( of == ForgeDirection.EAST || of == ForgeDirection.WEST )
				{
					AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
					FlipableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST );
					ico.setFlip( false, true );
				}

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
				Tessellator.instance.setColorOpaque_I( myColor.blackVariant );
				rh.setTexture( defa, defa, defa, defa, defa, defa );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( myColor.whiteVariant );
				rh.setTexture( defb, defb, defb, defb, defb, defb );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
			}

			rh.setTexture( getTexture( getCableColor() ) );
		}

		else if ( ghh != null && ccph != null && ghh.getCableConnectionType( of ) != AECableType.GLASS && ccph.getColor() != AEColor.Transparent
				&& ccph.getPart( of.getOpposite() ) == null )
			rh.setTexture( getSmartTexture( myColor = ccph.getColor() ) );
		else
			rh.setTexture( getSmartTexture( getCableColor() ) );

		switch (of)
		{
		case DOWN:
			rh.setBounds( 6, 0, 6, 10, 5, 10 );
			break;
		case EAST:
			rh.setBounds( 11, 6, 6, 16, 10, 10 );
			break;
		case NORTH:
			rh.setBounds( 6, 6, 0, 10, 10, 5 );
			break;
		case SOUTH:
			rh.setBounds( 6, 6, 11, 10, 10, 16 );
			break;
		case UP:
			rh.setBounds( 6, 11, 6, 10, 16, 10 );
			break;
		case WEST:
			rh.setBounds( 0, 6, 6, 5, 10, 10 );
			break;
		default:
			return;
		}

		rh.renderBlock( x, y, z, renderer );
		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );

		if ( !isGlass )
		{
			setSmartConnectionRotations( of, renderer );

			IIcon defa = new TaughtIcon( getChannelTex( channels, false ).getIcon(), -0.2f );
			IIcon defb = new TaughtIcon( getChannelTex( channels, true ).getIcon(), -0.2f );

			Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
			Tessellator.instance.setColorOpaque_I( myColor.blackVariant );
			rh.setTexture( defa, defa, defa, defa, defa, defa );
			renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			Tessellator.instance.setColorOpaque_I( myColor.whiteVariant );
			rh.setTexture( defb, defb, defb, defb, defb, defb );
			renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		}

	}

	@SideOnly(Side.CLIENT)
	protected void setSmartConnectionRotations(ForgeDirection of, RenderBlocks renderer)
	{
		switch (of)
		{
		case UP:
		case DOWN:
			renderer.uvRotateTop = 0;
			renderer.uvRotateBottom = 0;
			renderer.uvRotateSouth = 3;
			renderer.uvRotateEast = 3;
			break;
		case NORTH:
		case SOUTH:
			renderer.uvRotateTop = 3;
			renderer.uvRotateBottom = 3;
			renderer.uvRotateNorth = 1;
			renderer.uvRotateSouth = 2;
			renderer.uvRotateWest = 1;
			break;
		case EAST:
		case WEST:
			renderer.uvRotateEast = 2;
			renderer.uvRotateWest = 1;
			renderer.uvRotateBottom = 2;
			renderer.uvRotateTop = 1;
			renderer.uvRotateSouth = 3;
			renderer.uvRotateNorth = 0;
			break;
		default:
			break;

		}

	}

	@SideOnly(Side.CLIENT)
	protected void renderAllFaces(AEBaseBlock blk, int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( (float) renderer.renderMinX * 16.0f, (float) renderer.renderMinY * 16.0f, (float) renderer.renderMinZ * 16.0f,
				(float) renderer.renderMaxX * 16.0f, (float) renderer.renderMaxY * 16.0f, (float) renderer.renderMaxZ * 16.0f );
		rh.renderFace( x, y, z, blk.getRendererInstance().getTexture( ForgeDirection.WEST ), ForgeDirection.WEST, renderer );
		rh.renderFace( x, y, z, blk.getRendererInstance().getTexture( ForgeDirection.EAST ), ForgeDirection.EAST, renderer );
		rh.renderFace( x, y, z, blk.getRendererInstance().getTexture( ForgeDirection.NORTH ), ForgeDirection.NORTH, renderer );
		rh.renderFace( x, y, z, blk.getRendererInstance().getTexture( ForgeDirection.SOUTH ), ForgeDirection.SOUTH, renderer );
		rh.renderFace( x, y, z, blk.getRendererInstance().getTexture( ForgeDirection.DOWN ), ForgeDirection.DOWN, renderer );
		rh.renderFace( x, y, z, blk.getRendererInstance().getTexture( ForgeDirection.UP ), ForgeDirection.UP, renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimpliedRendering( x, y, z, this, renderCache );
		boolean useCovered = false;
		boolean requireDetailed = false;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart p = getHost().getPart( dir );
			if ( p != null && p instanceof IGridHost )
			{
				IGridHost igh = (IGridHost) p;
				AECableType type = igh.getCableConnectionType( dir.getOpposite() );
				if ( type == AECableType.COVERED || type == AECableType.SMART )
				{
					useCovered = true;
					break;
				}
			}
			else if ( connections.contains( dir ) )
			{
				TileEntity te = this.tile.getWorldObj().getTileEntity( x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ );
				IPartHost ccph = te instanceof IPartHost ? (IPartHost) te : null;
				IGridHost gh = te instanceof IGridHost ? (IGridHost) te : null;
				if ( ccph == null && gh != null && gh.getCableConnectionType( dir ) != AECableType.GLASS )
					requireDetailed = true;
			}
		}

		if ( useCovered )
		{
			rh.setTexture( getCoveredTexture( getCableColor() ) );
		}
		else
		{
			rh.setTexture( getTexture( getCableColor() ) );
		}

		IPartHost ph = getHost();
		for (ForgeDirection of : EnumSet.complementOf( connections ))
		{
			IPart bp = ph.getPart( of );
			if ( bp instanceof IGridHost )
			{
				int len = bp.cableConnectionRenderTo();
				if ( len < 8 )
				{
					switch (of)
					{
					case DOWN:
						rh.setBounds( 6, len, 6, 10, 6, 10 );
						break;
					case EAST:
						rh.setBounds( 10, 6, 6, 16 - len, 10, 10 );
						break;
					case NORTH:
						rh.setBounds( 6, 6, len, 10, 10, 6 );
						break;
					case SOUTH:
						rh.setBounds( 6, 6, 10, 10, 10, 16 - len );
						break;
					case UP:
						rh.setBounds( 6, 10, 6, 10, 16 - len, 10 );
						break;
					case WEST:
						rh.setBounds( len, 6, 6, 6, 10, 10 );
						break;
					default:
						continue;
					}
					rh.renderBlock( x, y, z, renderer );
				}
			}
		}

		if ( connections.size() != 2 || !nonLinear( connections ) || useCovered || requireDetailed )
		{
			if ( useCovered )
			{
				rh.setBounds( 5, 5, 5, 11, 11, 11 );
				rh.renderBlock( x, y, z, renderer );
			}
			else
			{
				rh.setBounds( 6, 6, 6, 10, 10, 10 );
				rh.renderBlock( x, y, z, renderer );
			}

			for (ForgeDirection of : connections)
			{
				rendereGlassConection( x, y, z, rh, renderer, of );
			}
		}
		else
		{
			IIcon def = getTexture( getCableColor() );
			rh.setTexture( def );

			for (ForgeDirection of : connections)
			{
				rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of, of.getOpposite() ) ) );
				switch (of)
				{
				case DOWN:
				case UP:
					renderer.setRenderBounds( 6 / 16.0, 0, 6 / 16.0, 10 / 16.0, 16 / 16.0, 10 / 16.0 );
					break;
				case EAST:
				case WEST:
					renderer.uvRotateEast = renderer.uvRotateWest = 1;
					renderer.uvRotateBottom = renderer.uvRotateTop = 1;
					renderer.setRenderBounds( 0, 6 / 16.0, 6 / 16.0, 16 / 16.0, 10 / 16.0, 10 / 16.0 );
					break;
				case NORTH:
				case SOUTH:
					renderer.uvRotateNorth = renderer.uvRotateSouth = 1;
					renderer.setRenderBounds( 6 / 16.0, 6 / 16.0, 0, 10 / 16.0, 10 / 16.0, 16 / 16.0 );
					break;
				default:
					continue;
				}
			}

			rh.renderBlockCurrentBounds( x, y, z, renderer );
		}

		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
		rh.setTexture( null );
	}

	@Override
	public boolean changeColor(AEColor newColor, EntityPlayer who)
	{
		if ( getCableColor() != newColor )
		{
			ItemStack newPart = null;

			if ( getCableConnectionType() == AECableType.GLASS )
				newPart = AEApi.instance().parts().partCableGlass.stack( newColor, 1 );
			else if ( getCableConnectionType() == AECableType.COVERED )
				newPart = AEApi.instance().parts().partCableCovered.stack( newColor, 1 );
			else if ( getCableConnectionType() == AECableType.SMART )
				newPart = AEApi.instance().parts().partCableSmart.stack( newColor, 1 );
			else if ( getCableConnectionType() == AECableType.DENSE )
				newPart = AEApi.instance().parts().partCableDense.stack( newColor, 1 );

			boolean hasPermission = true;

			try
			{
				hasPermission = proxy.getSecurity().hasPermission( who, SecurityPermissions.BUILD );
			}
			catch (GridAccessException e)
			{
				// :P
			}

			if ( newPart != null && hasPermission )
			{
				if ( Platform.isClient() )
					return true;

				getHost().removePart( ForgeDirection.UNKNOWN, true );
				getHost().addPart( newPart, ForgeDirection.UNKNOWN, who );
				return true;
			}
		}
		return false;
	}

	@Override
	public void setValidSides(EnumSet<ForgeDirection> sides)
	{
		proxy.setValidSides( sides );
	}

	protected boolean nonLinear(EnumSet<ForgeDirection> sides)
	{
		return (sides.contains( ForgeDirection.EAST ) && sides.contains( ForgeDirection.WEST ))
				|| (sides.contains( ForgeDirection.NORTH ) && sides.contains( ForgeDirection.SOUTH ))
				|| (sides.contains( ForgeDirection.UP ) && sides.contains( ForgeDirection.DOWN ));
	}

}
