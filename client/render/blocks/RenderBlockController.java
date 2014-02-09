package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraTextures;
import appeng.tile.networking.TileController;

public class RenderBlockController extends BaseBlockRender
{

	public RenderBlockController() {
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{

		boolean xx = world.getTileEntity( x - 1, y, z ) instanceof TileController && world.getTileEntity( x + 1, y, z ) instanceof TileController;
		boolean yy = world.getTileEntity( x, y - 1, z ) instanceof TileController && world.getTileEntity( x, y + 1, z ) instanceof TileController;
		boolean zz = world.getTileEntity( x, y, z - 1 ) instanceof TileController && world.getTileEntity( x, y, z + 1 ) instanceof TileController;

		int meta = world.getBlockMetadata( x, y, z );
		boolean hasPower = meta > 0;
		boolean isConflict = meta == 2;

		ExtraTextures lights = null;

		if ( xx && !yy && !zz )
		{
			if ( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerColumnPowered.getIcon() );
				if ( isConflict )
					lights = ExtraTextures.BlockControllerColumnConflict;
				else
					lights = ExtraTextures.BlockControllerColumnLights;
			}
			else
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerColumn.getIcon() );

			renderer.uvRotateEast = 1;
			renderer.uvRotateWest = 1;
			renderer.uvRotateTop = 1;
			renderer.uvRotateBottom = 1;
		}
		else if ( !xx && yy && !zz )
		{
			if ( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerColumnPowered.getIcon() );
				if ( isConflict )
					lights = ExtraTextures.BlockControllerColumnConflict;
				else
					lights = ExtraTextures.BlockControllerColumnLights;
			}
			else
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerColumn.getIcon() );

			renderer.uvRotateEast = 0;
			renderer.uvRotateNorth = 0;
		}
		else if ( !xx && !yy && zz )
		{
			if ( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerColumnPowered.getIcon() );
				if ( isConflict )
					lights = ExtraTextures.BlockControllerColumnConflict;
				else
					lights = ExtraTextures.BlockControllerColumnLights;
			}
			else
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerColumn.getIcon() );

			renderer.uvRotateNorth = 1;
			renderer.uvRotateSouth = 1;
			renderer.uvRotateTop = 0;
		}
		else if ( (xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) >= 2 )
		{
			int v = (Math.abs( x ) + Math.abs( y ) + Math.abs( z )) % 2;
			renderer.uvRotateEast = renderer.uvRotateBottom = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

			if ( v == 0 )
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerInsideA.getIcon() );
			else
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerInsideB.getIcon() );
		}
		else
		{
			if ( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraTextures.BlockControllerPowered.getIcon() );
				if ( isConflict )
					lights = ExtraTextures.BlockControllerConflict;
				else
					lights = ExtraTextures.BlockControllerLights;
			}
			else
				blk.getRendererInstance().setTemporaryRenderIcon( null );

		}

		boolean out = renderer.renderStandardBlock( blk, x, y, z );
		if ( lights != null )
		{
			Tessellator.instance.setColorOpaque_F( 1.0f, 1.0f, 1.0f );
			Tessellator.instance.setBrightness( 14 << 20 | 14 << 4 );
			renderer.renderFaceXNeg( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceXPos( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceYNeg( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceYPos( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceZNeg( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceZPos( blk, x, y, z, lights.getIcon() );
		}

		blk.getRendererInstance().setTemporaryRenderIcon( null );
		renderer.uvRotateEast = renderer.uvRotateBottom = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		return out;
	}
}
