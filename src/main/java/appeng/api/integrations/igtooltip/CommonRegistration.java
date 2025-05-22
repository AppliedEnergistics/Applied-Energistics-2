package appeng.api.integrations.igtooltip;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface CommonRegistration {
    <T extends BlockEntity> void addBlockEntityData(Class<T> blockEntityClass, ServerDataProvider<? super T> provider);

}
