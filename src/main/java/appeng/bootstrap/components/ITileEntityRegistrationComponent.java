package appeng.bootstrap.components;

import appeng.bootstrap.IBootstrapComponent;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

@FunctionalInterface
public interface ITileEntityRegistrationComponent extends IBootstrapComponent {
    void register(IForgeRegistry<TileEntityType<?>> registry);
}
