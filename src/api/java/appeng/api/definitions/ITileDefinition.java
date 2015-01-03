package appeng.api.definitions;


import net.minecraft.tileentity.TileEntity;

import com.google.common.base.Optional;


public interface ITileDefinition extends IBlockDefinition
{
	/**
	 * @return the {@link TileEntity} Class if applicable.
	 */
	Optional<? extends Class<? extends TileEntity>> maybeEntity();
}
