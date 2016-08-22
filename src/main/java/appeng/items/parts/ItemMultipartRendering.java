package appeng.items.parts;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;


public class ItemMultipartRendering extends ItemRenderingCustomizer
{

	private final ItemMultiPart item;

	public ItemMultipartRendering( ItemMultiPart item )
	{
		this.item = item;
	}

	@Override
	public void customize( IItemRendering rendering )
	{
		rendering.meshDefinition( this::getItemMeshDefinition );
	}

	private ModelResourceLocation getItemMeshDefinition( ItemStack is )
	{
		// TODO: Avoid object creation here. This happens every frame
		return new ModelResourceLocation( item.getTypeByStack( is ).getModel(), null );
	}

}
