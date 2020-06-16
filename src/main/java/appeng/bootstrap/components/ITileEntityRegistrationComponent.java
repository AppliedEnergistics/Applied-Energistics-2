package appeng.bootstrap.components;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface ITileEntityRegistrationComponent extends IBootstrapComponent {
    void register(IForgeRegistry<TileEntityType<?>> registry);
}
