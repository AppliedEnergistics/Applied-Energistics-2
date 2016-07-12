
package appeng.client.render.renderable;


import java.nio.FloatBuffer;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;


public class ItemRenderable<T extends TileEntity> implements Renderable<T>
{

	private final Function<T, Pair<ItemStack, Matrix4f>> f;

	public ItemRenderable( Function<T, Pair<ItemStack, Matrix4f>> f )
	{
		this.f = f;
	}

	@Override
	public void renderTileEntityAt( T te, double x, double y, double z, float partialTicks, int destroyStage )
	{
		Pair<ItemStack, Matrix4f> pair = f.apply( te );
		if( pair != null && pair.getLeft() != null )
		{
			GlStateManager.pushMatrix();
			if( pair.getRight() != null )
			{
				FloatBuffer matrix = BufferUtils.createFloatBuffer( 16 );
				pair.getRight().store( matrix );
				matrix.flip();
				GlStateManager.multMatrix( matrix );
			}
			Minecraft.getMinecraft().getRenderItem().renderItem( pair.getLeft(), TransformType.GROUND );
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void renderTileEntityFast( T te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer )
	{

	}

}
