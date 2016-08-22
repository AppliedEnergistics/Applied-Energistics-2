package appeng.bootstrap.components;


import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;


/**
 * Registers a custom item mesh definition that can be used to dynamically determine the item model based on
 * item stack properties.
 */
public class ItemMeshDefinitionComponent implements InitComponent
{

	private final Item item;

	private final ItemMeshDefinition meshDefinition;

	public ItemMeshDefinitionComponent( @Nonnull Item item, @Nonnull ItemMeshDefinition meshDefinition )
	{
		this.item = item;
		this.meshDefinition = meshDefinition;
	}

	@Override
	public void initialize( Side side )
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, meshDefinition );
	}
}
