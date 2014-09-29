package appeng.parts.networking;

import java.util.EnumSet;

import appeng.client.texture.FlippableIcon;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.texture.OffsetIcon;
import appeng.client.texture.TaughtIcon;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartCableSmart extends PartCable
{

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

	public PartCableSmart(Class c, ItemStack is) {
		super( c, is );
	}

	public PartCableSmart(ItemStack is) {
		this( PartCableSmart.class, is );
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.SMART;
	}

	@Override
	public IIcon getTexture(AEColor c)
	{
		return getSmartTexture( c );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glTranslated( -0.0, -0.0, 0.3 );

		float offU = 0;
		float offV = 9;

		OffsetIcon main = new OffsetIcon( getTexture( getCableColor() ), offU, offV );
		OffsetIcon ch1 = new OffsetIcon( getChannelTex( 4, false ).getIcon(), offU, offV );
		OffsetIcon ch2 = new OffsetIcon( getChannelTex( 4, true ).getIcon(), offU, offV );

		for (ForgeDirection side : EnumSet.of( ForgeDirection.UP, ForgeDirection.DOWN ))
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
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
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		main = new OffsetIcon( getTexture( getCableColor() ), 0, 0 );
		ch1 = new OffsetIcon( getChannelTex( 4, false ).getIcon(), 0, 0 );
		ch2 = new OffsetIcon( getChannelTex( 4, true ).getIcon(), 0, 0 );

		for (ForgeDirection side : EnumSet.of( ForgeDirection.SOUTH, ForgeDirection.NORTH ))
		{
			rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
			rh.renderInventoryFace( main, side, renderer );
			rh.renderInventoryFace( ch1, side, renderer );
			rh.renderInventoryFace( ch2, side, renderer );
		}

		rh.setTexture( null );
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 5.0, 5.0, 5.0, 11.0, 11.0, 11.0 );

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

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimplifiedRendering( x, y, z, this, renderCache );
		rh.setTexture( getTexture( getCableColor() ) );

		EnumSet<ForgeDirection> sides = connections.clone();

		boolean hasBuses = false;
		IPartHost ph = getHost();
		for (ForgeDirection of : EnumSet.complementOf( connections ))
		{
			IPart bp = ph.getPart( of );
			if ( bp instanceof IGridHost )
			{
				if ( of != ForgeDirection.UNKNOWN )
				{
					sides.add( of );
					hasBuses = true;
				}

				int len = bp.cableConnectionRenderTo();
				if ( len < 8 )
				{
					switch (of)
					{
					case DOWN:
						rh.setBounds( 6, len, 6, 10, 5, 10 );
						break;
					case EAST:
						rh.setBounds( 11, 6, 6, 16 - len, 10, 10 );
						break;
					case NORTH:
						rh.setBounds( 6, 6, len, 10, 10, 5 );
						break;
					case SOUTH:
						rh.setBounds( 6, 6, 11, 10, 10, 16 - len );
						break;
					case UP:
						rh.setBounds( 6, 11, 6, 10, 16 - len, 10 );
						break;
					case WEST:
						rh.setBounds( len, 6, 6, 5, 10, 10 );
						break;
					default:
						continue;
					}
					rh.renderBlock( x, y, z, renderer );

					setSmartConnectionRotations( of, renderer );
					IIcon firstIcon = new TaughtIcon( getChannelTex( channelsOnSide[of.ordinal()], false ).getIcon(), -0.2f );
					IIcon secondIcon = new TaughtIcon( getChannelTex( channelsOnSide[of.ordinal()], true ).getIcon(), -0.2f );

					if ( of == ForgeDirection.EAST || of == ForgeDirection.WEST )
					{
						AEBaseBlock blk = (AEBaseBlock) rh.getBlock();
						FlippableIcon ico = blk.getRendererInstance().getTexture( ForgeDirection.EAST );
						ico.setFlip( false, true );
					}

					Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
					Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
					rh.setTexture( firstIcon, firstIcon, firstIcon, firstIcon, firstIcon, firstIcon );
					renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
					rh.setTexture( secondIcon, secondIcon, secondIcon, secondIcon, secondIcon, secondIcon );
					renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

					renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

					rh.setTexture( getTexture( getCableColor() ) );
				}
			}
		}

		if ( sides.size() != 2 || !nonLinear( sides ) || hasBuses )
		{
			for (ForgeDirection of : connections)
			{
				renderSmartConnection( x, y, z, rh, renderer, channelsOnSide[of.ordinal()], of );
			}

			rh.setTexture( getCoveredTexture( getCableColor() ) );
			rh.setBounds( 5, 5, 5, 11, 11, 11 );
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

			IIcon firstTaughtIcon = new TaughtIcon( getChannelTex( channels, false ).getIcon(), -0.2f );
			IIcon firstOffsetIcon = new OffsetIcon( firstTaughtIcon, 0, -12 );

			IIcon secondTaughtIcon = new TaughtIcon( getChannelTex( channels, true ).getIcon(), -0.2f );
			IIcon secondOffsetIcon = new OffsetIcon( secondTaughtIcon, 0, -12 );

			switch (selectedSide)
			{
			case DOWN:
			case UP:
				renderer.setRenderBounds( 5 / 16.0, 0, 5 / 16.0, 11 / 16.0, 16 / 16.0, 11 / 16.0 );
				rh.setTexture( def, def, off, off, off, off );
				rh.renderBlockCurrentBounds( x, y, z, renderer );

				renderer.uvRotateTop = 0;
				renderer.uvRotateBottom = 0;
				renderer.uvRotateSouth = 3;
				renderer.uvRotateEast = 3;

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

				Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
				rh.setTexture( firstTaughtIcon, firstTaughtIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
				rh.setTexture( secondTaughtIcon, secondTaughtIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon );
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

				renderer.setRenderBounds( 0, 5 / 16.0, 5 / 16.0, 16 / 16.0, 11 / 16.0, 11 / 16.0 );
				rh.renderBlockCurrentBounds( x, y, z, renderer );

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

				FlippableIcon fpA = new FlippableIcon( firstTaughtIcon );
				FlippableIcon fpB = new FlippableIcon( secondTaughtIcon );

				fpA = new FlippableIcon( firstTaughtIcon );
				fpB = new FlippableIcon( secondTaughtIcon );

				fpA.setFlip( true, false );
				fpB.setFlip( true, false );

				Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
				rh.setTexture( firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstOffsetIcon, firstTaughtIcon, fpA );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
				rh.setTexture( secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondOffsetIcon, secondTaughtIcon, fpB );
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
				renderer.setRenderBounds( 5 / 16.0, 5 / 16.0, 0, 11 / 16.0, 11 / 16.0, 16 / 16.0 );
				rh.renderBlockCurrentBounds( x, y, z, renderer );

				Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );

				Tessellator.instance.setColorOpaque_I( getCableColor().blackVariant );
				rh.setTexture( firstOffsetIcon, firstOffsetIcon, firstTaughtIcon, firstTaughtIcon, firstOffsetIcon, firstOffsetIcon );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );

				Tessellator.instance.setColorOpaque_I( getCableColor().whiteVariant );
				rh.setTexture( secondOffsetIcon, secondOffsetIcon, secondTaughtIcon, secondTaughtIcon, secondOffsetIcon, secondOffsetIcon );
				renderAllFaces( (AEBaseBlock) rh.getBlock(), x, y, z, rh, renderer );
				break;
			default:
				break;
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}
}
