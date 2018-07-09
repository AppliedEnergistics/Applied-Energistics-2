package appeng.bootstrap.components;

import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface IEntityRegistrationComponent extends IBootstrapComponent
{
	void entityRegistration( IForgeRegistry<EntityEntry> entityRegistry );
}
