
package appeng.api.definitions;


import java.util.Optional;

import net.minecraft.tileentity.TileEntity;


public interface ITileDefinition extends IBlockDefinition
{
	/**
	 * @return the {@link TileEntity} Class if applicable.
	 */
	Optional<? extends Class<? extends TileEntity>> maybeEntity();
}
