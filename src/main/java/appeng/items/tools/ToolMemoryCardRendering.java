package appeng.items.tools;


import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.model.MemoryCardModel;
import appeng.core.AppEng;


public class ToolMemoryCardRendering extends ItemRenderingCustomizer
{

	private static final ResourceLocation MODEL = new ResourceLocation( AppEng.MOD_ID, "builtin/memory_card" );

	@Override
	@OnlyIn( Dist.CLIENT )
	public void customize( IItemRendering rendering )
	{
		rendering.builtInModel( "models/item/builtin/memory_card", new MemoryCardModel() );
		rendering.model( new ModelResourceLocation( MODEL, "inventory" ) ).variants( MODEL );
	}
}
