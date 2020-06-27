package appeng.bootstrap.components;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface ITileEntityRegistrationComponent extends IBootstrapComponent {
    void register(IForgeRegistry<BlockEntityType<?>> registry);
}
