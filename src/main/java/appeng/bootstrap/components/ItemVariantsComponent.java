package appeng.bootstrap.components;


import java.util.Collection;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;


public class ItemVariantsComponent implements IBootstrapComponent
{

	private final Item item;

	private final Collection<ResourceLocation> resources;

	public ItemVariantsComponent( Item item, Collection<ResourceLocation> resources )
	{
		this.item = item;
		this.resources = resources;
	}

	@Override
	public void preInitialize( Side side )
	{
		ResourceLocation[] resourceArr = resources.toArray( new ResourceLocation[0] );
		ModelBakery.registerItemVariants( item, resourceArr );
	}
}
