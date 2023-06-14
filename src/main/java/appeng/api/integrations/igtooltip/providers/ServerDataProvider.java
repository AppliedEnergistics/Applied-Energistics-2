package appeng.api.integrations.igtooltip.providers;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

@ApiStatus.NonExtendable
@ApiStatus.OverrideOnly
@FunctionalInterface
public interface ServerDataProvider<T> {
    void provideServerData(Player player, T object, CompoundTag serverData);
}
