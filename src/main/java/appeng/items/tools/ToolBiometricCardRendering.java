package appeng.items.tools;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.client.render.model.BiometricCardModel;
import appeng.core.AppEng;


public class ToolBiometricCardRendering extends ItemRenderingCustomizer
{

	public static final ResourceLocation MODEL = new ResourceLocation( AppEng.MOD_ID, "builtin/biometric_card" );

	@Override
	public void customize( IItemRendering rendering )
	{
		rendering.builtInModel( "models/item/builtin/biometric_card", new BiometricCardModel() );
		rendering.model( new ModelResourceLocation( MODEL, "inventory" ) ).variants( MODEL );
	}
}
