package appeng.bootstrap.components;


import appeng.bootstrap.IBootstrapComponent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;


@FunctionalInterface
public interface IEntityRegistrationComponent extends IBootstrapComponent {
    void entityRegistration(IForgeRegistry<EntityEntry> entityRegistry);
}
