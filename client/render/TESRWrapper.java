package appeng.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import appeng.block.AEBaseBlock;
import appeng.tile.AEBaseTile;
import cpw.mods.fml.common.FMLLog;

public class TESRWrapper extends TileEntitySpecialRenderer
{

	final public RenderBlocks rbinstance = new RenderBlocks();

	final BaseBlockRender blkRender;
	final double MAX_DISTANCE;

	public TESRWrapper(BaseBlockRender render) {
		blkRender = render;
		MAX_DISTANCE = blkRender.getTesrRenderDistance();
	}

	@Override
	final public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f)
	{
		if ( te instanceof AEBaseTile )
		{
			Block b = te.getBlockType();

			if ( b instanceof AEBaseBlock && ((AEBaseTile) te).requiresTESR() )
			{
				if ( Math.abs( x ) > MAX_DISTANCE || Math.abs( y ) > MAX_DISTANCE || Math.abs( z ) > MAX_DISTANCE )
					return;

				Tessellator tess = Tessellator.instance;

				if ( tess.isDrawing )
					return;

				try
				{
					GL11.glPushMatrix();
					GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

					rbinstance.blockAccess = te.worldObj;
					blkRender.renderTile( (AEBaseBlock) b, (AEBaseTile) te, tess, x, y, z, f, rbinstance );

					if ( tess.isDrawing )
						throw new RuntimeException( "Error durring rendering." );

					GL11.glPopAttrib();
					GL11.glPopMatrix();
				}
				catch (Throwable t)
				{
					FMLLog.severe( "Hi, Looks like there was a crash while rendering something..." );
					t.printStackTrace();
					FMLLog.severe( "MC will now crash ( probobly )!" );
					throw new RuntimeException( t );
				}

			}
		}
	}
}
