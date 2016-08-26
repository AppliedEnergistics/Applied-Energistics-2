package appeng.items.parts;


import java.util.Arrays;
import java.util.stream.Collectors;

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

		// Register all item models as variants so they get loaded
		rendering.variants( Arrays.stream( PartType.values() )
				.flatMap( part -> part.getItemModels().stream() )
				.collect( Collectors.toList() ) );
	}

	private ModelResourceLocation getItemMeshDefinition( ItemStack is )
	{
		PartType partType = item.getTypeByStack( is );
		int variant = item.variantOf( is.getItemDamage() );
		return partType.getItemModels().get( variant );
	}
}
