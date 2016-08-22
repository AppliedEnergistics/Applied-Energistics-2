package appeng.bootstrap;


import java.util.Arrays;
import java.util.Collection;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Allows the rendering of an item to be customized.
 */
public interface IItemRendering
{

	/**
	 * Registers a custom item mesh definition that will be used to dynamically determine the
	 * item model to be used for rendering by inspecting the item stack (i.e. for NBT data).
	 * Please
	 */
	@SideOnly( Side.CLIENT )
	IItemRendering meshDefinition( ItemMeshDefinition meshDefinition );

	/**
	 * Registers an item model for meta=0, see {@link #model(int, ModelResourceLocation)}.
	 */
	@SideOnly( Side.CLIENT )
	default IItemRendering model( ModelResourceLocation model )
	{
		return model( 0, model );
	}

	/**
	 * Registers an item model for a given meta.
	 */
	@SideOnly( Side.CLIENT )
	IItemRendering model( int meta, ModelResourceLocation model );

	/**
	 * Convenient override for {@link #variants(Collection)}.
	 */
	@SideOnly( Side.CLIENT )
	default IItemRendering variants( ResourceLocation... resources )
	{
		return variants( Arrays.asList( resources ) );
	}

	/**
	 * Registers the item variants of this item. This are all models that need to be loaded for this item.
	 * This has no direct effect on rendering, but is used to load models that are used for example by
	 * the ItemMeshDefinition.
	 *
	 * Models registered via {@link #model(int, ModelResourceLocation)} are automatically added here.
	 */
	@SideOnly( Side.CLIENT )
	IItemRendering variants( Collection<ResourceLocation> resources );

	/**
	 * Registers a custom item color definition that inspects an item stack and tint and
	 * returns a color multiplier.
	 */
	@SideOnly( Side.CLIENT )
	IItemRendering color( IItemColor itemColor );
}
