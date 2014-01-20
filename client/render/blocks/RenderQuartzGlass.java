package appeng.client.render.blocks;

import java.util.Random;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraTextures;
import appeng.client.texture.OffsetIcon;

public class RenderQuartzGlass extends BaseBlockRender
{

	static byte offsets[][][];

	public RenderQuartzGlass() {
		super( false, 0 );
		if ( offsets == null )
		{
			Random r = new Random( 924 );
			offsets = new byte[10][10][10];
			for (int x = 0; x < 10; x++)
				for (int y = 0; y < 10; y++)
					r.nextBytes( offsets[x][y] );
		}
	}

	boolean isFlush(AEBaseBlock imb, IBlockAccess world, int x, int y, int z)
	{
		return isGlass( imb, world, x, y, z );
	}

	boolean isGlass(AEBaseBlock imb, IBlockAccess world, int x, int y, int z)
	{
		return world.getBlockId( x, y, z ) == AEApi.instance().blocks().blockQuartzGlass.block().blockID
				|| world.getBlockId( x, y, z ) == AEApi.instance().blocks().blockQuartzVibrantGlass.block().blockID;
	}

	void renderEdge(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer, ForgeDirection side, ForgeDirection direction)
	{
		if ( !isFlush( imb, world, x + side.offsetX, y + side.offsetY, z + side.offsetZ ) )
		{
			if ( !isFlush( imb, world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ ) )
			{
				float minX = 0.5f + (side.offsetX + direction.offsetX) / 2.0f;
				float minY = 0.5f + (side.offsetY + direction.offsetY) / 2.0f;
				float minZ = 0.5f + (side.offsetZ + direction.offsetZ) / 2.0f;
				float maxX = 0.5f + (side.offsetX + direction.offsetX) / 2.0f;
				float maxY = 0.5f + (side.offsetY + direction.offsetY) / 2.0f;
				float maxZ = 0.5f + (side.offsetZ + direction.offsetZ) / 2.0f;

				if ( 0 == side.offsetX && 0 == direction.offsetX )
				{
					minX = 0.0f;
					maxX = 1.0f;
				}
				if ( 0 == side.offsetY && 0 == direction.offsetY )
				{
					minY = 0.0f;
					maxY = 1.0f;
				}
				if ( 0 == side.offsetZ && 0 == direction.offsetZ )
				{
					minZ = 0.0f;
					maxZ = 1.0f;
				}

				if ( maxX <= 0.001f )
					maxX += 0.9f / 16.0f;
				if ( maxY <= 0.001f )
					maxY += 0.9f / 16.0f;
				if ( maxZ <= 0.001f )
					maxZ += 0.9f / 16.0f;

				if ( minX >= 0.999f )
					minX -= 0.9f / 16.0f;
				if ( minY >= 0.999f )
					minY -= 0.9f / 16.0f;
				if ( minZ >= 0.999f )
					minZ -= 0.9f / 16.0f;

				renderer.setRenderBounds( minX, minY, minZ, maxX, maxY, maxZ );

				switch (side)
				{
				case WEST:
					renderer.renderFaceXNeg( imb, x, y, z, ExtraTextures.GlassFrame.getIcon() );
					break;
				case EAST:
					renderer.renderFaceXPos( imb, x, y, z, ExtraTextures.GlassFrame.getIcon() );
					break;
				case NORTH:
					renderer.renderFaceZNeg( imb, x, y, z, ExtraTextures.GlassFrame.getIcon() );
					break;
				case SOUTH:
					renderer.renderFaceZPos( imb, x, y, z, ExtraTextures.GlassFrame.getIcon() );
					break;
				case DOWN:
					renderer.renderFaceYNeg( imb, x, y, z, ExtraTextures.GlassFrame.getIcon() );
					break;
				case UP:
					renderer.renderFaceYPos( imb, x, y, z, ExtraTextures.GlassFrame.getIcon() );
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer)
	{
		renderer.overrideBlockTexture = ExtraTextures.GlassFrame.getIcon();
		super.renderInventory( block, is, renderer );
		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		int cx = Math.abs( x % 10 );
		int cy = Math.abs( y % 10 );
		int cz = Math.abs( z % 10 );

		int u = offsets[cx][cy][cz] % 4;
		int v = offsets[9 - cx][9 - cy][9 - cz] % 4;

		switch (Math.abs( (offsets[cx][cy][cz] + (x + y + z)) % 4 ))
		{
		case 0:
			renderer.overrideBlockTexture = new OffsetIcon( imb.getIcon( 0, 0 ), u / 2, v / 2 );
			break;
		case 1:
			renderer.overrideBlockTexture = new OffsetIcon( ExtraTextures.BlockQuartzGlassB.getIcon(), u / 2, v / 2 );
			break;
		case 2:
			renderer.overrideBlockTexture = new OffsetIcon( ExtraTextures.BlockQuartzGlassC.getIcon(), u, v );
			break;
		case 3:
			renderer.overrideBlockTexture = new OffsetIcon( ExtraTextures.BlockQuartzGlassD.getIcon(), u, v );
			break;
		}

		boolean result = renderer.renderStandardBlock( imb, x, y, z );

		renderer.overrideBlockTexture = null;
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.EAST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.WEST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.NORTH );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.SOUTH );

		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.EAST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.WEST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.NORTH );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.SOUTH );

		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.UP );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.DOWN );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.NORTH );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.SOUTH );

		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.UP );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.DOWN );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.NORTH );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.SOUTH );

		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.EAST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.WEST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.UP );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.DOWN );

		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.EAST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.WEST );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.UP );
		renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.DOWN );

		return result;
	}

}
