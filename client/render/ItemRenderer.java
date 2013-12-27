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
		if ( type == ItemRenderType.ENTITY )
			GL11.glTranslatef( -0.5f, -0.5f, -0.5f );
		if ( type == ItemRenderType.INVENTORY )
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );
		WorldRender.instance.renderItemBlock( item );
		GL11.glPopMatrix();
	}

}
