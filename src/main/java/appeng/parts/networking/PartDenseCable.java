package appeng.parts.networking;

import java.util.EnumSet;

import appeng.client.texture.FlippableIcon;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.OffsetIcon;
import appeng.client.texture.TaughtIcon;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartDenseCable extends PartCable
{

	@Override
	public BusSupport supportsBuses()
	{
		return BusSupport.DENSE_CABLE;
	}

	@MENetworkEventSubscribe
	public void channelUpdated(MENetworkChannelsChanged c)
	{
		getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		getHost().markForUpdate();
	}

	public PartDenseCable(Class c, ItemStack is) {
		super( c, is );
		proxy.setFlags( GridFlags.DENSE_CAPACITY, GridFlags.PREFERRED );
	}

	public PartDenseCable(ItemStack is) {
		this( PartDenseCable.class, is );
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.DENSE;
	}

	@Override
	public IIcon getTexture(AEColor c)
	{
		if ( c == AEColor.Transparent )
			return AEApi.instance().parts().partCableSmart.stack( AEColor.Transparent, 1 ).getIconIndex();

		return getSmartTexture( c );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );
		rh.setBounds( 4.0f, 4.0f, 2.0f, 12.0f, 12.0f, 14.0f );

		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( getTexture( getCableColor() ), offU, offV );
		OffsetIcon ch1 = new OffsetIcon( getChannelTex( 4, false ).getIcon(), offU, offV );
		OffsetIcon ch2 = new OffsetIcon( getChannelTex( 4, true ).getIcon(), offU, offV );

		for (ForgeDirection side : EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ))
		{
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		offU = 9;
		offV = 0;
		main = new OffsetIcon( getTexture( getCableColor() ), offU, offV );
		ch1 = new OffsetIcon( getChannelTex( 4, false ).getIcon(), offU, offV );
		ch2 = new OffsetIcon( getChannelTex( 4, true ).getIcon(), offU, offV );

		for (ForgeDirection side : EnumSet.of( ForgeDirection.EAST, ForgeDirection.WEST ))
		{
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		main = new OffsetIcon( getTexture( getCableColor() ), 0, 0 );
		ch1 = new OffsetIcon( getChannelTex( 4, false ).getIcon(), 0, 0 );
		ch2 = new OffsetIcon( getChannelTex( 4, true ).getIcon(), 0, 0 );

		for (ForgeDirection side : EnumSet.of( ForgeDirection.SOUTH, ForgeDirection.NORTH ))
		{
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		boolean noLadder = !bch.isBBCollision();
		double min = noLadder ? 3.0 : 4.9;
		double max = noLadder ? 13.0 : 11.1;

		bch.addBox( min, min, min, max, max, max );

		if ( Platform.isServer() )
		{
			IGridNode n = getGridNode();
			if ( n != null )
				connections = n.getConnectedSides();
			else
				connections.clear();
		}

		for (ForgeDirection of : connections)
		{
			if ( isDense( of ) )
			{
				switch (of)
				{
				case DOWN:
					bch.addBox( min, 0.0, min, max, min, max );
					break;
				case EAST:
					bch.addBox( max, min, min, 16.0, max, max );
					break;
				case NORTH:
					bch.addBox( min, min, 0.0, max, max, min );
					break;
				case SOUTH:
					bch.addBox( min, min, max, max, max, 16.0 );
					break;
				case UP:
					bch.addBox( min, max, min, max, 16.0, max );
					break;
				case WEST:
					bch.addBox( 0.0, min, min, min, max, max );
					break;
				default:
					continue;
				}
			}
			else
			{
				switch (of)
				{
				case DOWN:
					bch.addBox( 5.0, 0.0, 5.0, 11.0, 5.0, 11.0 );
					break;
				case EAST:
					bch.addBox( 11.0, 5.0, 5.0, 16.0, 11.0, 11.0 );
					break;
				case NORTH:
					bch.addBox( 5.0, 5.0, 0.0, 11.0, 11.0, 5.0 );
					break;
				case SOUTH:
					bch.addBox( 5.0, 5.0, 11.0, 11.0, 11.0, 16.0 );
					break;
				case UP:
					bch.addBox( 5.0, 11.0, 5.0, 11.0, 16.0, 11.0 );
					break;
				case WEST:
					bch.addBox( 0.0, 5.0, 5.0, 5.0, 11.0, 11.0 );
					break;
				default:
					continue;
				}
			}
		}
	}

	private boolean isDense(ForgeDirection of)
	{
		TileEntity te = tile.getWorldObj().getTileEntity( tile.xCoord + of.offsetX, tile.yCoord + of.offsetY, tile.zCoord + of.offsetZ );
		if ( te instanceof IGridHost )
		{
			AECableType t = ((IGridHost) te).getCableConnectionType( of.getOpposite() );
			return t == AECableType.DENSE;
		}
		return false;
	}

	private boolean isSmart(ForgeDirection of)
	{
		TileEntity te = tile.getWorldObj().getTileEntity( tile.xCoord + of.offsetX, tile.yCoord + of.offsetY, tile.zCoord + of.offsetZ );
		if ( te instanceof IGridHost )
		{
			AECableType t = ((IGridHost) te).getCableConnectionType( of.getOpposite() );
			return t == AECableType.SMART;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public void renderDenseConnection(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer, int channels, ForgeDirection of)
	{
		TileEntity te = this.tile.getWorldObj().getTileEntity( x + of.offsetX, y + of.offsetY, z + of.offsetZ );
		IPartHost partHost = te instanceof IPartHost ? (IPartHost) te : null;
		IGridHost ghh = te instanceof IGridHost ? (IGridHost) te : null;
		boolean isGlass = false;
		AEColor myColor = getCableColor();
		/*
		 * ( ghh != null && partHost != null && ghh.getCableConnectionType( of ) == AECableType.GLASS && partHost.getPart(
		 * of.getOpposite() ) == null ) { isGlass = true; rh.setTexture( getGlassTexture( myColor = partHost.getColor() ) );
		 * } else if ( partHost == null && ghh != null && ghh.getCableConnectionType( of ) != AECableType.GLASS ) {
		 * rh.setTexture( getSmartTexture( myColor ) ); switch (of) { case DOWN: rh.setBounds( 3, 0, 3, 13, 4, 13 );
		 * break; case EAST: rh.setBounds( 12, 3, 3, 16, 13, 13 ); break; case NORTH: rh.setBounds( 3, 3, 0, 13, 13, 4
		 * ); break; case SOUTH: rh.setBounds( 3, 3, 12, 13, 13, 16 ); break; case UP: rh.setBounds( 3, 12, 3, 13, 16,
		 * 13 ); break; case WEST: rh.setBounds( 0, 3, 3, 4, 13, 13 ); break; default: return; } rh.renderBlock( x, y,
		 * z, renderer );
		 * 
		 * if ( true ) { setSmartConnectionRotations( of, renderer ); IIcon firstIcon = new TaughtIcon( getChannelTex(
		 * channels, false ).getIcon(), -0.2f ); IIcon secondIcon = new TaughtIcon( getChannelTex( channels, true ).getIcon(),
		 * -0.2f );
		 * 
		 * if ( of == ForgeDirection.EAST || of == ForgeDirection.WEST ) { AEBaseBlock blk = (AEBaseBlock)
		 * rh.getBlock(); FlippableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST ); ico.setFlip(
		 * false, true ); }
		 * 
		 * Tessellator.instance.setBrightness( 15 << 20 | 15 << 5 ); Tessellator.instance.setColorOpaque_I(
		 * myColor.mediumVariant ); rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon ); renderAllFaces( (AEBaseBlock)
		 * rh.getBlock(), x, y, z, renderer );
		 * 
		 * Tessellator.instance.setColorOpaque_I( myColor.whiteVariant ); rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon,
		 * secondIcon ); renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, renderer );
		 * 
		 * renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth =
		 * renderer.uvRotateTop = renderer.uvRotateWest = 0; }
		 * 
		 * rh.setTexture( getTexture( getCableColor() ) ); }
		 */

		rh.setFacesToRender( EnumSet.complementOf( EnumSet.of( of, of.getOpposite() ) ) );
		if ( ghh != null && partHost != null && ghh.getCableConnectionType( of ) != AECableType.GLASS && partHost.getColor() != AEColor.Transparent
				&& partHost.getPart( of.getOpposite() ) == null )
			rh.setTexture( getTexture( myColor = partHost.getColor() ) );
		else
			rh.setTexture( getTexture( getCableColor() ) );

		switch (of)
		{
		case DOWN:
			rh.setBounds( 4, 0, 4, 12, 5, 12 );
			break;
		case EAST:
			rh.setBounds( 11, 4, 4, 16, 12, 12 );
			break;
		case NORTH:
			rh.setBounds( 4, 4, 0, 12, 12, 5 );
			break;
		case SOUTH:
			rh.setBounds( 4, 4, 11, 12, 12, 16 );
			break;
		case UP:
			rh.setBounds( 4, 11, 4, 12, 16, 12 );
			break;
		case WEST:
			rh.setBounds( 0, 4, 4, 5, 12, 12 );
			break;
		default:
			return;
		}

		rh.renderBlock( x, y, z, renderer );

		rh.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
		if ( !isGlass )
		{
			setSmartConnectionRotations( of, renderer );

			IIcon firstIcon = new TaughtIcon( getChannelTex( channels, false ).getIcon(), -0.2f );
			IIcon secondIcon = new TaughtIcon( getChannelTex( channels, true ).getIcon(), -0.2f );

			Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
			Tessellator.instance.setColorOpaque_I( myColor.blackVariant );
			rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
			renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			Tessellator.instance.setColorOpaque_I( myColor.whiteVariant );
			rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
			renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

			renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimplifiedRendering( x, y, z, this, renderCache );
		rh.setTexture( getTexture( getCableColor() ) );

		EnumSet<ForgeDirection> sides = connections.clone();

		boolean hasBuses = false;
		for (ForgeDirection of : connections)
		{
			if ( !isDense( of ) )
				hasBuses = true;
		}

		if ( sides.size() != 2 || !nonLinear( sides ) || hasBuses )
		{
			for (ForgeDirection of : connections)
			{
				if ( isDense( of ) )
					renderDenseConnection( x, y, z, rh, renderer, channelsOnSide[of.ordinal()], of );
				else if ( isSmart( of ) )
					renderSmartConnection( x, y, z, rh, renderer, channelsOnSide[of.ordinal()], of );
				else
					renderCoveredConnection( x, y, z, rh, renderer, channelsOnSide[of.ordinal()], of );
			}

			rh.setTexture( getDenseTexture( getCableColor() ) );
			rh.setBounds( 3, 3, 3, 13, 13, 13 );
			rh.renderBlock( x, y, z, renderer );
		}
		else
		{
			ForgeDirection selectedSide = ForgeDirection.UNKNOWN;

			for (ForgeDirection of : connections)
			{
				selectedSide = of;
				break;
			}

			int channels = channelsOnSide[selectedSide.ordinal()];
			IIcon def = getTexture( getCableColor() );
			IIcon off = new OffsetIcon( def, 0, -12 );

			IIcon firstIcon = new TaughtIcon( getChannelTex( channels, false ).getIcon(), -0.2f );
			IIcon firstOffset = new OffsetIcon( firstIcon, 0, -12 );

			IIcon secondIcon = new TaughtIcon( getChannelTex( channels, true ).getIcon(), -0.2f );
			IIcon secondOffset = new OffsetIcon( secondIcon, 0, -12 );

			switch (selectedSide)
			{
			case DOWN:
			case UP:
				renderer.setRenderBounds( 3 / 16.0, 0, 3 / 16.0, 13 / 16.0, 16 / 16.0, 13 / 16.0 );
				rh.setTexture( def, def, off, off, off, off );
				rh.renderBlockCurrentBounds( x, y, z, renderer );

				renderer.uvRotateTop = 0;
				renderer.uvRotateBottom = 0;
				renderer.uvRotateSouth = 3;
				renderer.uvRotateEast = 3;

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

				Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
				rh.setTexture( firstIcon, firstIcon, firstOffset, firstOffset, firstOffset, firstOffset );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
				rh.setTexture( secondIcon, secondIcon, secondOffset, secondOffset, secondOffset, secondOffset );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
				break;
			case EAST:
			case WEST:
				rh.setTexture( off, off, off, off, def, def );
				renderer.uvRotateEast = 2;
				renderer.uvRotateWest = 1;
				renderer.uvRotateBottom = 2;
				renderer.uvRotateTop = 1;
				renderer.uvRotateSouth = 0;
				renderer.uvRotateNorth = 0;

				AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
				FlippableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST );
				ico.setFlip( false, true );

				renderer.setRenderBounds( 0, 3 / 16.0, 3 / 16.0, 16 / 16.0, 13 / 16.0, 13 / 16.0 );
				rh.renderBlockCurrentBounds( x, y, z, renderer );

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

				FlippableIcon fpA = new FlippableIcon( firstIcon );
				FlippableIcon fpB = new FlippableIcon( secondIcon );

				fpA.setFlip( true, false );
				fpB.setFlip( true, false );

				Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
				rh.setTexture( firstOffset, firstOffset, firstOffset, firstOffset, firstIcon, fpA );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
				rh.setTexture( secondOffset, secondOffset, secondOffset, secondOffset, secondIcon, fpB );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
				break;
			case NORTH:
			case SOUTH:
				rh.setTexture( off, off, def, def, off, off );
				renderer.uvRotateTop = 3;
				renderer.uvRotateBottom = 3;
				renderer.uvRotateNorth = 1;
				renderer.uvRotateSouth = 2;
				renderer.uvRotateWest = 1;
				renderer.setRenderBounds( 3 / 16.0, 3 / 16.0, 0, 13 / 16.0, 13 / 16.0, 16 / 16.0 );
				rh.renderBlockCurrentBounds( x, y, z, renderer );

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

				Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
				rh.setTexture( firstOffset, firstOffset, firstIcon, firstIcon, firstOffset, firstOffset );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
				rh.setTexture( secondOffset, secondOffset, secondIcon, secondIcon, secondOffset, secondOffset );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
				break;
			default:
				break;
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}

	private IIcon getDenseTexture(AEColor c)
	{
		switch (c)
		{
		case Black:
			return CableBusTextures.MEDense_Black.getIcon();
		case Blue:
			return CableBusTextures.MEDense_Blue.getIcon();
		case Brown:
			return CableBusTextures.MEDense_Brown.getIcon();
		case Cyan:
			return CableBusTextures.MEDense_Cyan.getIcon();
		case Gray:
			return CableBusTextures.MEDense_Gray.getIcon();
		case Green:
			return CableBusTextures.MEDense_Green.getIcon();
		case LightBlue:
			return CableBusTextures.MEDense_LightBlue.getIcon();
		case LightGray:
			return CableBusTextures.MEDense_LightGrey.getIcon();
		case Lime:
			return CableBusTextures.MEDense_Lime.getIcon();
		case Magenta:
			return CableBusTextures.MEDense_Magenta.getIcon();
		case Orange:
			return CableBusTextures.MEDense_Orange.getIcon();
		case Pink:
			return CableBusTextures.MEDense_Pink.getIcon();
		case Purple:
			return CableBusTextures.MEDense_Purple.getIcon();
		case Red:
			return CableBusTextures.MEDense_Red.getIcon();
		case White:
			return CableBusTextures.MEDense_White.getIcon();
		case Yellow:
			return CableBusTextures.MEDense_Yellow.getIcon();
		default:
		}

		return is.getIconIndex();
	}
}
