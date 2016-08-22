package appeng.bootstrap;


import net.minecraftforge.fml.relauncher.Side;


/**
 * Bootstrap components can be registered to take part in the various initialization phases of Forge.
 */
public interface IBootstrapComponent
{

	default void preInitialize( Side side )
	{
	}

	default void initialize( Side side )
	{
	}

	default void postInitialize( Side side )
	{
	}
}
