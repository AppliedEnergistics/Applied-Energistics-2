package appeng.client.render.items;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.input.Keyboard;

import appeng.util.Platform;

public class ItemEncodedPatternRenderer implements IItemRenderer
{

	RenderItem ri = new RenderItem();
	boolean resursive;

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		boolean isShiftHeld = Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );

		if ( resursive == false && type == IItemRenderer.ItemRenderType.INVENTORY && isShiftHeld )
		{
			if ( readOutput( item ) != null )
				return true;
		}

		return false;
	}

	private ItemStack readOutput(ItemStack item)
	{
		NBTTagCompound encodedValue = item.getTagCompound();

		if ( encodedValue == null )
			return null;

		NBTTagList outTag = encodedValue.getTagList( "out", 10 );

		if ( outTag.tagCount() == 0 )
			return null;

		ItemStack out = null;

		for (int x = 0; x < outTag.tagCount(); x++)
		{
			ItemStack readItem = ItemStack.loadItemStackFromNBT( outTag.getCompoundTagAt( x ) );
			if ( readItem != null )
			{
				if ( out == null )
					out = readItem;
				else if ( out != null && Platform.isSameItemPrecise( readItem, out ) )
					out.stackSize += readItem.stackSize;
			}
		}

		return out;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		resursive = true;

		ItemStack is = readOutput( item );
		// data is just render blocks.

		// ri.renderItemAndEffectIntoGUI( par1FontRenderer, par2TextureManager, par3ItemStack, par4, par5 );

		resursive = false;
	}
}
