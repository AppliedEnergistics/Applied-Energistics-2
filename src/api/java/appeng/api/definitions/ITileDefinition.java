package appeng.api.definitions;


import net.minecraft.tileentity.TileEntity;


public interface ITileDefinition extends IBlockDefinition
{
	/**
	 * @return the {@link TileEntity} Class if applicable.
	 */
	Class<? extends TileEntity> entity();
}
