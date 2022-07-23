package appeng.integration.modules.jade.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import snownee.jade.api.ITooltip;

import appeng.api.parts.IPart;

/**
 * Interface for part data providers that only work with server-side data.
 */
public interface IPartDataProvider {
    default void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
    }

    default void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
    }
}
