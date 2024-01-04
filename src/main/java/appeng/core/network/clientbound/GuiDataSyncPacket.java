
package appeng.core.network.clientbound;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.network.ClientboundPacket;
import appeng.menu.AEBaseMenu;

/**
 * This packet is used to synchronize menu-fields from server to client.
 */
public record GuiDataSyncPacket(int containerId, byte[] syncData) implements ClientboundPacket {
    public GuiDataSyncPacket(int containerId, Consumer<FriendlyByteBuf> writer) {
        this(containerId, createSyncData(writer));
    }

    private static byte[] createSyncData(Consumer<FriendlyByteBuf> writer) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        writer.accept(buffer);
        var result = new byte[buffer.readableBytes()];
        buffer.readBytes(result);
        return result;
    }

    public static GuiDataSyncPacket decode(FriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var syncData = data.readByteArray();
        return new GuiDataSyncPacket(containerId, syncData);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeVarInt(containerId);
        data.writeByteArray(syncData);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu baseMenu && c.containerId == this.containerId) {
            baseMenu.receiveServerSyncData(new FriendlyByteBuf(Unpooled.wrappedBuffer(this.syncData)));
        }
    }

}
