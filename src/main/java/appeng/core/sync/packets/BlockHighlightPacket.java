package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;
import appeng.client.render.BlockHighlightHandler;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.BasePacket;
import appeng.menu.me.crafting.CraftingCPUMenu;

public class BlockHighlightPacket extends BasePacket {
    private final BlockPos pos;
    private final ResourceKey<Level> level;
    private final long time;

    public BlockHighlightPacket(FriendlyByteBuf stream) {
        this.pos = stream.readBlockPos();
        this.level = stream.readResourceKey(Registries.DIMENSION);
        this.time = stream.readLong();
    }

    public BlockHighlightPacket(BlockPos pos, ResourceKey<Level> level, long time) {
        this.pos = pos;
        this.level = level;
        this.time = time;

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeBlockPos(pos);
        data.writeResourceKey(level);
        data.writeLong(time);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(Player player) {
        BlockHighlightHandler.highlight(pos, level, time);
        player.sendSystemMessage(PlayerMessages.HighlightedBlock.text(
                Component.literal("§b" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                Component.literal("§a" + level.location())));
    }

    public static class HighlightWhat extends BasePacket {
        private final AEKey what;

        public HighlightWhat(FriendlyByteBuf stream) {
            this.what = AEKey.readKey(stream);
        }

        public HighlightWhat(AEKey what) {
            this.what = what;

            var data = new FriendlyByteBuf(Unpooled.buffer());
            data.writeInt(this.getPacketID());
            AEKey.writeKey(data, what);
            this.configureWrite(data);
        }

        @Override
        public void serverPacketData(ServerPlayer player) {
            var menu = player.containerMenu;
            if (menu instanceof CraftingCPUMenu cpuMenu) {
                cpuMenu.highlight(what);
            }
        }
    }
}
