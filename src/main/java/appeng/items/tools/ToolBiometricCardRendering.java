package appeng.items.tools;


import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.model.BiometricCardModel;
import appeng.core.AppEng;


public class ToolBiometricCardRendering extends ItemRenderingCustomizer
{

	private static final ResourceLocation MODEL = new ResourceLocation( AppEng.MOD_ID, "builtin/biometric_card" );

	@Override
	@OnlyIn( Dist.CLIENT )
	public void customize( IItemRendering rendering )
	{
		rendering.builtInModel( "models/item/builtin/biometric_card", new BiometricCardModel() );
		rendering.model( new ModelResourceLocation( MODEL, "inventory" ) ).variants( MODEL );
	}
}
