
package appeng.bootstrap.components;

import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface IEntityRegistrationComponent extends IBootstrapComponent {
    void entityRegistration(IForgeRegistry<EntityType<?>> entityRegistry);
}
