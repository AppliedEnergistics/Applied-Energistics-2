package appeng.bootstrap.components;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import appeng.core.AppEng;


public class ModelOverrideComponent implements PreInitComponent
{

	// Maps from resource path to customizer
	private final Map<String, BiFunction<ModelResourceLocation, IBakedModel, IBakedModel>> customizer = new HashMap<>();

	public void addOverride( String resourcePath, BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer )
	{
		this.customizer.put( resourcePath, customizer );
	}

	@Override
	public void preInitialize( Side side )
	{
		MinecraftForge.EVENT_BUS.register( this );
	}

	@SubscribeEvent
	public void onModelBakeEvent( final ModelBakeEvent event )
	{
		IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		Set<ModelResourceLocation> keys = Sets.newHashSet( modelRegistry.getKeys() );

		for( ModelResourceLocation location : keys )
		{
			if( !location.getResourceDomain().equals( AppEng.MOD_ID ) )
			{
				continue;
			}

			BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer = this.customizer.get( location.getResourcePath() );
			if( customizer != null )
			{
				IBakedModel orgModel = modelRegistry.getObject( location );
				IBakedModel newModel = customizer.apply( location, orgModel );

				if( newModel != orgModel )
				{
					modelRegistry.putObject( location, newModel );
				}
			}
		}
	}
}
