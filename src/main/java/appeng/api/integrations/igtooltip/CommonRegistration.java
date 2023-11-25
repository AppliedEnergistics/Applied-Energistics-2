package appeng.api.integrations.igtooltip;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.providers.ServerDataProvider;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface CommonRegistration {
    <T extends BlockEntity> void addBlockEntityData(ResourceLocation id,
                                                    Class<T> blockEntityClass,
                                                    ServerDataProvider<? super T> provider);

}
