package appeng.api.parts;


import java.util.Arrays;
import java.util.Collection;

import net.minecraft.util.ResourceLocation;

import appeng.api.AEInjectable;


/**
 * Allows registration of part models that can then be used in {@link IPart#getStaticModels()}.
 */
@AEInjectable
public interface IPartModels
{

	/**
	 * Allows registration of part models that can then be used in {@link IPart#getStaticModels()}.
	 *
	 * Models can be registered multiple times without causing issues.
	 *
	 * This method must be called during the pre-init phase (as part of your plugin's constructor).
	 */
	void registerModels( Collection<ResourceLocation> partModels );

	/**
	 * Convenience overload of {@link #registerModels(Collection)}
	 */
	default void registerModels( ResourceLocation... partModels )
	{
		registerModels( Arrays.asList( partModels ) );
	}
}
