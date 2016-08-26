package appeng.bootstrap.components;


import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.client.render.model.BuiltInModelLoader;


@SideOnly( Side.CLIENT )
public class BuiltInModelComponent implements PreInitComponent
{

	private final Map<String, IModel> builtInModels = new HashMap<>();

	private boolean hasInitialized = false;

	public void addModel( String path, IModel model )
	{
		Preconditions.checkState( !hasInitialized );
		builtInModels.put( path, model );
	}

	@Override
	public void preInitialize( Side side )
	{
		hasInitialized = true;

		BuiltInModelLoader loader = new BuiltInModelLoader( builtInModels );
		ModelLoaderRegistry.registerLoader( loader );
	}
}
