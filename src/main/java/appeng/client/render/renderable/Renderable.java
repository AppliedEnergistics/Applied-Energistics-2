
package appeng.client.render.renderable;


import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;


public interface Renderable<T extends TileEntity>
{

	public void renderTileEntityAt( T te, double x, double y, double z, float partialTicks, int destroyStage );

	public void renderTileEntityFast( T te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer );

}
