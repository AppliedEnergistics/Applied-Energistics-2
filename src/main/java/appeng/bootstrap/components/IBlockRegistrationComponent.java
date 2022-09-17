package appeng.bootstrap.components;


import appeng.bootstrap.IBootstrapComponent;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;


@FunctionalInterface
public interface IBlockRegistrationComponent extends IBootstrapComponent {
    void blockRegistration(Side side, IForgeRegistry<Block> blockRegistry);
}
