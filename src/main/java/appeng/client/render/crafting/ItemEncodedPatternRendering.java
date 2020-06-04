
package appeng.client.render.crafting;


import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.core.AppEng;


public class ItemEncodedPatternRendering extends ItemRenderingCustomizer
{

	private static final ResourceLocation MODEL = new ResourceLocation( AppEng.MOD_ID, "builtin/encoded_pattern" );

	@Override
	@OnlyIn( Dist.CLIENT )
	public void customize( IItemRendering rendering )
	{
		// FIXME rendering.builtInModel( "models/item/builtin/encoded_pattern", new ItemEncodedPatternModel() );
		// FIXME rendering.model( new ModelResourceLocation( MODEL, "inventory" ) ).variants( MODEL );
	}

}
