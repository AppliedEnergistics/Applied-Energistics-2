package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.core.AELog;

/**
 * Locates a {@link net.minecraft.world.level.block.entity.BlockEntity} that hosts a menu.
 */
record BlockEntityLocator(BlockPos pos) implements MenuLocator {
    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static BlockEntityLocator readFromPacket(FriendlyByteBuf buf) {
        return new BlockEntityLocator(buf.readBlockPos());
    }

    @Override
    public <T> @Nullable T locate(Player player, Class<T> hostInterface) {
        var blockEntity = player.level.getBlockEntity(pos);

        if (hostInterface.isInstance(blockEntity)) {
            return hostInterface.cast(blockEntity);
        } else if (blockEntity != null) {
            AELog.warn("Cannot locate menu host @ %s, %s does not implement %s",
                    pos, blockEntity, hostInterface);
        }

        return null;
    }

    @Override
    public String toString() {
        return "BlockEntity{pos=" + pos + '}';
    }
}
