package appeng.bootstrap.components;


import java.util.Map;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;


/**
 * Registers the models that should by used for an item, including the ability to
 * distinguish by meta.
 */
public class ItemModelComponent implements InitComponent
{

	private final Item item;

	private final Map<Integer, ModelResourceLocation> modelsByMeta;

	public ItemModelComponent( @Nonnull Item item, @Nonnull Map<Integer, ModelResourceLocation> modelsByMeta )
	{
		this.item = item;
		this.modelsByMeta = modelsByMeta;
	}

	@Override
	public void initialize( Side side )
	{
		ItemModelMesher itemMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		modelsByMeta.forEach( ( meta, model ) ->
		{
			itemMesher.register( item, meta, model );
		} );
	}
	
}
