package appeng.core.features.registries;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import appeng.api.parts.IPartModels;


public class PartModels implements IPartModels
{

	private final Set<ResourceLocation> models = new HashSet<>();

	private boolean initialized = false;

	@Override
	public void registerModels( Collection<ResourceLocation> partModels )
	{
		if( initialized )
		{
			throw new IllegalStateException( "Cannot register models after the pre-initialization phase!" );
		}

		models.addAll( partModels );
	}

	public Set<ResourceLocation> getModels()
	{
		return models;
	}

	public void setInitialized( boolean initialized )
	{
		this.initialized = initialized;
	}
}
