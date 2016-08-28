package appeng.items.parts;


import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.FacadeItemModel;


/**
 * Handles rendering customization for facade items. Please note that this works very differently
 * from actually rendering a Facade in a cable bus.
 */
public class FacadeRendering extends ItemRenderingCustomizer
{
	@Override
	public void customize( IItemRendering rendering )
	{
		// This actually just uses the path it will look for by default, no custom model redirection needed
		rendering.builtInModel( "models/item/facade", new FacadeItemModel() );
	}
}
