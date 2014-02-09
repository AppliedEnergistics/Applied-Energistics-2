package appeng.client.render;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class ItemRenderer implements IItemRenderer
{

	public static final ItemRenderer instance = new ItemRenderer();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glEnable( GL11.GL_ALPHA_TEST );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

		if ( type == ItemRenderType.ENTITY )
			GL11.glTranslatef( -0.5f, -0.5f, -0.5f );
		if ( type == ItemRenderType.INVENTORY )
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );
		WorldRender.instance.renderItemBlock( item );

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

}
