package appeng.client.render.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import appeng.items.misc.ItemEncodedPattern;

public class ItemEncodedPatternRenderer implements IItemRenderer
{

	final RenderItem ri = new RenderItem();
	boolean recursive;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		boolean isShiftHeld = Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );

		if ( recursive == false && type == IItemRenderer.ItemRenderType.INVENTORY && isShiftHeld )
		{
			ItemEncodedPattern iep = (ItemEncodedPattern) item.getItem();
			if ( iep.getOutput( item ) != null )
				return true;
		}

		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		recursive = true;

		ItemEncodedPattern iep = (ItemEncodedPattern) item.getItem();

		ItemStack is = iep.getOutput( item );
		Minecraft mc = Minecraft.getMinecraft();

		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		RenderHelper.enableGUIStandardItemLighting();
		ri.renderItemAndEffectIntoGUI( mc.fontRenderer, mc.getTextureManager(), is, 0, 0 );
		RenderHelper.disableStandardItemLighting();
		GL11.glPopAttrib();

		recursive = false;
	}
}
