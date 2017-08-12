
package appeng.bootstrap.components;


import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface IBlockRegistrationComponent extends IBootstrapComponent
{
	void blockRegistration( Side side, IForgeRegistry<Block> blockRegistry );
}
