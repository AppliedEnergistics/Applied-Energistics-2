
package appeng.bootstrap.components;

import net.fabricmc.api.EnvType;
import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface IBlockRegistrationComponent extends IBootstrapComponent {
    void blockRegistration(EnvType dist, IForgeRegistry<Block> blockRegistry);
}
