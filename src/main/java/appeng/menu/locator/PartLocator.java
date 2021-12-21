package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.api.parts.PartHelper;
import appeng.core.AELog;

/**
 * Locates a part on an {@link appeng.api.parts.IPartHost} using the position of its host and the side it's attached to.
 */
record PartLocator(BlockPos pos, @Nullable Direction side) implements MenuLocator {
    @Override
    public <T> @Nullable T locate(Player player, Class<T> hostInterface) {
        var part = PartHelper.getPart(player.level, pos, side);
        if (hostInterface.isInstance(part)) {
            return hostInterface.cast(part);
        } else if (part != null) {
            AELog.warn("Part at %s does not implement host interface %s",
                    part, hostInterface);
        }

        return null;
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(side != null);
        if (side != null) {
            buf.writeByte(side.ordinal());
        }
    }

    public static PartLocator readFromPacket(FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        Direction side = null;
        if (buf.readBoolean()) {
            side = Direction.values()[buf.readByte()];
        }
        return new PartLocator(pos, side);
    }
}
