package appeng.core.features.registries;

import java.util.HashSet;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import appeng.api.features.IWorldGen;

public class WorldGenRegistry implements IWorldGen
{

	private static class TypeSet
	{

		final HashSet<Class<? extends WorldProvider>> badProviders = new HashSet<Class<? extends WorldProvider>>();
		final HashSet<Integer> enabledDimensions = new HashSet<Integer>();

	}

	final TypeSet[] types;

	static final public WorldGenRegistry instance = new WorldGenRegistry();

	private WorldGenRegistry() {

		types = new TypeSet[WorldGenType.values().length];

		for (WorldGenType type : WorldGenType.values())
		{
			types[type.ordinal()] = new TypeSet();
		}

	}

	@Override
	public boolean isWorldGenEnabled(WorldGenType type, World w)
	{
		if ( type == null )
			throw new IllegalArgumentException( "Bad Type Passed" );

		if ( w == null )
			throw new IllegalArgumentException( "Bad Provider Passed" );

		if ( !types[type.ordinal()].badProviders.contains( w.provider.getClass() )
				&& types[type.ordinal()].enabledDimensions.contains( w.provider.dimensionId ) )
		{
			return true;
		}

		return false;
	}

	@Override
	public void disableWorldGenForProviderID(WorldGenType type, Class<? extends WorldProvider> provider)
	{
		if ( type == null )
			throw new IllegalArgumentException( "Bad Type Passed" );

		if ( provider == null )
			throw new IllegalArgumentException( "Bad Provider Passed" );

		types[type.ordinal()].badProviders.add( provider );
	}

	@Override
	public void enableWorldGenForDimension(WorldGenType type, int dimensionID)
	{
		if ( type == null )
			throw new IllegalArgumentException( "Bad Type Passed" );

		types[type.ordinal()].enabledDimensions.add( dimensionID );
	}

}
