
package appeng.client.render.tesr;


import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.client.render.FacingToRotation;
import appeng.client.render.renderable.Renderable;
import appeng.tile.AEBaseTile;

@SideOnly( Side.CLIENT )
public class ModularTESR<T extends AEBaseTile> extends TileEntitySpecialRenderer<T>
{

	private final Renderable[] renderables;

	public ModularTESR( Renderable... renderables )
	{
		this.renderables = renderables;
	}

	@Override
	public void renderTileEntityAt( T te, double x, double y, double z, float partialTicks, int destroyStage )
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate( x, y, z );
		GlStateManager.translate( 0.5, 0.5, 0.5 );
		FacingToRotation.get( te.getForward(), te.getUp() ).glRotateCurrentMat();
		GlStateManager.translate( -0.5, -0.5, -0.5 );
		for( Renderable renderable : renderables )
		{
			renderable.renderTileEntityAt( te, x, y, z, partialTicks, destroyStage );
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void renderTileEntityFast( T te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer )
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate( x, y, z );
		GlStateManager.translate( 0.5, 0.5, 0.5 );
		FacingToRotation.get( te.getForward(), te.getUp() ).glRotateCurrentMat();
		GlStateManager.translate( -0.5, -0.5, -0.5 );
		for( Renderable renderable : renderables )
		{
			renderable.renderTileEntityFast( te, x, y, z, partialTicks, destroyStage, buffer );
		}
		GlStateManager.popMatrix();
	}

}