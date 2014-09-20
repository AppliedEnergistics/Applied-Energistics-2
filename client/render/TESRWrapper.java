package appeng.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import appeng.block.AEBaseBlock;
import appeng.core.AELog;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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

				if ( Platform.isDrawing( tess ) )
					return;

				try
				{
					GL11.glPushMatrix();
					GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

					rbinstance.blockAccess = te.getWorldObj();
					blkRender.renderTile( (AEBaseBlock) b, (AEBaseTile) te, tess, x, y, z, f, rbinstance );

					if ( Platform.isDrawing( tess ) )
						throw new RuntimeException( "Error during rendering." );

					GL11.glPopAttrib();
					GL11.glPopMatrix();
				}
				catch (Throwable t)
				{
					AELog.severe( "Hi, Looks like there was a crash while rendering something..." );
					t.printStackTrace();
					AELog.severe( "MC will now crash ( probobly )!" );
					throw new RuntimeException( t );
				}

			}
		}
	}
}
