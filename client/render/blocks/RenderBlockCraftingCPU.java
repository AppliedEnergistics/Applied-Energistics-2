package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingTile;

public class RenderBlockCraftingCPU extends BaseBlockRender
{

	protected RenderBlockCraftingCPU(boolean useTesr, int range) {
		super( useTesr, range );
	}

	public RenderBlockCraftingCPU() {
		super( false, 20 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		super.renderInventory( blk, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock blk, IBlockAccess w, int x, int y, int z, RenderBlocks renderer)
	{
		IIcon theIcon = null;
		boolean formed = false;
		boolean emitsLight = false;

		TileCraftingTile ct = blk.getTileEntity( w, x, y, z );
		if ( ct != null && ct.isFormed() )
		{
			formed = true;
			emitsLight = ct.isPowered();
		}
		int meta = w.getBlockMetadata( x, y, z ) & 3;

		boolean isMonitor = blk.getClass() == BlockCraftingMonitor.class;
		theIcon = blk.getIcon( ForgeDirection.SOUTH.ordinal(), meta | (formed ? 8 : 0) );

		IIcon nonForward = theIcon;
		if ( isMonitor )
			nonForward = AEApi.instance().blocks().blockCraftingUnit.block().getIcon( 0, meta | (formed ? 8 : 0) );

		if ( formed )
		{
			renderer = BusRenderer.instance.renderer;
			BusRenderHelper i = BusRenderHelper.instance;
			BusRenderer.instance.renderer.isFacade = true;

			renderer.blockAccess = w;
			i.setPass( 0 );
			i.setOrientation( ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH );

			try
			{
				ct.lightCache = i.useSimpliedRendering( x, y, z, null, ct.lightCache );
			}
			catch (Throwable t)
			{

			}

			float highX = isConnected( w, x, y, z, ForgeDirection.EAST ) ? 16 : 13.01f;
			float lowX = isConnected( w, x, y, z, ForgeDirection.WEST ) ? 0 : 2.99f;

			float highY = isConnected( w, x, y, z, ForgeDirection.UP ) ? 16 : 13.01f;
			float lowY = isConnected( w, x, y, z, ForgeDirection.DOWN ) ? 0 : 2.99f;

			float highZ = isConnected( w, x, y, z, ForgeDirection.SOUTH ) ? 16 : 13.01f;
			float lowZ = isConnected( w, x, y, z, ForgeDirection.NORTH ) ? 0 : 2.99f;

			renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.NORTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.SOUTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.NORTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.SOUTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.NORTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.SOUTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.NORTH );
			renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.SOUTH );

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				i.setBounds( fso( side, lowX, ForgeDirection.WEST ), fso( side, lowY, ForgeDirection.DOWN ), fso( side, lowZ, ForgeDirection.NORTH ),
						fso( side, highX, ForgeDirection.EAST ), fso( side, highY, ForgeDirection.UP ), fso( side, highZ, ForgeDirection.SOUTH ) );
				i.prepareBounds( renderer );

				boolean LocalEmit = emitsLight;
				if ( blk instanceof BlockCraftingMonitor && !ct.getForward().equals( side ) )
					LocalEmit = false;

				handleSide( blk, meta, x, y, z, i, renderer, ct.getForward().equals( side ) ? theIcon : nonForward, LocalEmit, isMonitor, side, w );
			}

			BusRenderer.instance.renderer.isFacade = false;
			i.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
			i.normalRendering();

			return true;
		}
		else
		{
			double a = 0.0 / 16.0;
			double o = 16.0 / 16.0;
			renderer.setRenderBounds( a, a, a, o, o, o );
			boolean out = renderer.renderStandardBlock( blk, x, y, z );

			return out;
		}
	}

	private void renderCorner(BusRenderHelper i, RenderBlocks renderer, IBlockAccess w, int x, int y, int z, ForgeDirection up, ForgeDirection east,
			ForgeDirection south)
	{
		if ( isConnected( w, x, y, z, up ) )
			return;
		if ( isConnected( w, x, y, z, east ) )
			return;
		if ( isConnected( w, x, y, z, south ) )
			return;

		i.setBounds( gso( east, 3, ForgeDirection.WEST ), gso( up, 3, ForgeDirection.DOWN ), gso( south, 3, ForgeDirection.NORTH ),
				gso( east, 13, ForgeDirection.EAST ), gso( up, 13, ForgeDirection.UP ), gso( south, 13, ForgeDirection.SOUTH ) );
		i.prepareBounds( renderer );
		i.setTexture( ExtraBlockTextures.BlockCraftingUnitRing.getIcon() );
		i.renderBlockCurrentBounds( x, y, z, renderer );
	}

	private float gso(ForgeDirection side, float def, ForgeDirection target)
	{
		if ( side != target )
		{
			if ( side.offsetX > 0 || side.offsetY > 0 || side.offsetZ > 0 )
				return 16;
			return 0;
		}
		return def;
	}

	private float fso(ForgeDirection side, float def, ForgeDirection target)
	{
		if ( side == target )
		{
			if ( side.offsetX > 0 || side.offsetY > 0 || side.offsetZ > 0 )
				return 16;
			return 0;
		}
		return def;
	}

	private void handleSide(AEBaseBlock blk, int meta, int x, int y, int z, BusRenderHelper i, RenderBlocks renderer, IIcon color, boolean emitsLight,
			boolean isMonitor, ForgeDirection side, IBlockAccess w)
	{
		if ( isConnected( w, x, y, z, side ) )
			return;

		i.setFacesToRender( EnumSet.of( side ) );

		if ( meta == 0 && blk.getClass() == BlockCraftingUnit.class )
		{
			i.setTexture( ExtraBlockTextures.BlockCraftingUnitFit.getIcon() );
			i.renderBlockCurrentBounds( x, y, z, renderer );
		}
		else
		{
			if ( color == ExtraBlockTextures.BlockCraftingMonitorFit.getIcon() )
				i.setTexture( ExtraBlockTextures.BlockCraftingMonitorOuter.getIcon() );
			else
				i.setTexture( ExtraBlockTextures.BlockCraftingFitSolid.getIcon() );

			i.renderBlockCurrentBounds( x, y, z, renderer );

			if ( color != null )
			{
				i.setTexture( color );

				if ( !emitsLight )
				{
					i.renderBlockCurrentBounds( x, y, z, renderer );
				}
				else
				{
					Tessellator.instance.setColorOpaque_F( 1.0f, 1.0f, 1.0f );
					Tessellator.instance.setBrightness( 13 << 20 | 13 << 4 );
					i.renderFace( x, y, z, color, side, renderer );
				}

			}
		}

		for (ForgeDirection a : ForgeDirection.VALID_DIRECTIONS)
		{
			if ( a == side || a == side.getOpposite() )
				continue;

			if ( (side.offsetX != 0 || side.offsetZ != 0)
					&& (a == ForgeDirection.NORTH || a == ForgeDirection.EAST || a == ForgeDirection.WEST || a == ForgeDirection.SOUTH) )
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			else if ( (side.offsetY != 0) && (a == ForgeDirection.EAST || a == ForgeDirection.WEST) )
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			else
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLong.getIcon() );

			double width = 3.0 / 16.0;

			if ( !(i.getBound( a ) < 0.001 || i.getBound( a ) > 15.999) )
			{
				switch (a)
				{
				case DOWN:
					renderer.renderMinY = 0;
					renderer.renderMaxY = width;
					break;
				case EAST:
					renderer.renderMaxX = 1;
					renderer.renderMinX = 1.0 - width;
					renderer.uvRotateTop = 1;
					renderer.uvRotateBottom = 1;
					renderer.uvRotateWest = 1;
					renderer.uvRotateEast = 1;
					break;
				case NORTH:
					renderer.renderMinZ = 0;
					renderer.renderMaxZ = width;
					renderer.uvRotateWest = 1;
					renderer.uvRotateNorth = 1;
					renderer.uvRotateSouth = 1;
					break;
				case SOUTH:
					renderer.renderMaxZ = 1;
					renderer.renderMinZ = 1.0 - width;
					renderer.uvRotateNorth = 1;
					renderer.uvRotateSouth = 1;
					break;
				case UP:
					renderer.renderMaxY = 1;
					renderer.renderMinY = 1.0 - width;
					break;
				case WEST:
					renderer.renderMinX = 0;
					renderer.renderMaxX = width;
					renderer.uvRotateTop = 1;
					renderer.uvRotateBottom = 1;
					renderer.uvRotateWest = 1;
					renderer.uvRotateEast = 1;
					break;
				case UNKNOWN:
				default:
				}

				i.renderBlockCurrentBounds( x, y, z, renderer );
				i.prepareBounds( renderer );
			}
		}
	}

	private boolean isConnected(IBlockAccess w, int x, int y, int z, ForgeDirection side)
	{
		return w.getTileEntity( x + side.offsetX, y + side.offsetY, z + side.offsetZ ) instanceof TileCraftingTile;
	}
}
