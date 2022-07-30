package appeng.api.integrations.igtooltip.providers;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

@ApiStatus.NonExtendable
@ApiStatus.OverrideOnly
@FunctionalInterface
public interface ServerDataProvider<T> {
    void provideServerData(ServerPlayer player, T object, CompoundTag serverData);
}
