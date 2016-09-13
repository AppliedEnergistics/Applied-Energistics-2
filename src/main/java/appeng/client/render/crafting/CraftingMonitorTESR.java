package appeng.client.render.crafting;


import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.TesrRenderHelper;
import appeng.tile.crafting.TileCraftingMonitorTile;


/**
 * Renders the item currently being crafted
 */
@SideOnly( Side.CLIENT )
public class CraftingMonitorTESR extends TileEntitySpecialRenderer<TileCraftingMonitorTile>
{

	@Override
	public void renderTileEntityAt( TileCraftingMonitorTile te, double x, double y, double z, float partialTicks, int destroyStage )
	{
		if( te == null )
		{
			return;
		}

		EnumFacing facing = te.getForward();

		IAEItemStack jobProgress = te.getJobProgress();
		if( jobProgress != null )
		{
			GlStateManager.pushMatrix();
			GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

			TesrRenderHelper.moveToFace( facing );
			TesrRenderHelper.rotateToFace( facing, (byte) 0 );
			TesrRenderHelper.renderItem2dWithAmount( jobProgress, 0.7f, 0.1f );

			GlStateManager.popMatrix();
		}
	}
}
