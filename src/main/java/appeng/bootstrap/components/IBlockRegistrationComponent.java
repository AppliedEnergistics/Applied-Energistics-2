
package appeng.bootstrap.components;


import net.minecraft.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface IBlockRegistrationComponent extends IBootstrapComponent
{
	void blockRegistration( Dist side, IForgeRegistry<Block> blockRegistry );
}
