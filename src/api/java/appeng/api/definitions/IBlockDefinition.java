package appeng.api.definitions;


import net.minecraft.block.Block;


public interface IBlockDefinition extends IItemDefinition
{
	/**
	 * @return the {@link Block} Implementation if applicable
	 */
	Block block();
}
