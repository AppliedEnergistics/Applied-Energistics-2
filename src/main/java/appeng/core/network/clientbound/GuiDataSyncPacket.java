
package appeng.core.network.clientbound;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.core.network.ClientboundPacket;
import appeng.menu.AEBaseMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * This packet is used to synchronize menu-fields from server to client.
 */
public record GuiDataSyncPacket(int containerId, FriendlyByteBuf syncData) implements ClientboundPacket {
    public GuiDataSyncPacket(int containerId, Consumer<FriendlyByteBuf> writer) {
        this(containerId, new FriendlyByteBuf(Unpooled.buffer()));
        writer.accept(syncData);
    }

    public static GuiDataSyncPacket decode(FriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var syncData = new FriendlyByteBuf(data.copy());
        return new GuiDataSyncPacket(containerId, syncData);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeVarInt(containerId);
        this.syncData.getBytes(0, data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu baseMenu && c.containerId == this.containerId) {
            baseMenu.receiveServerSyncData(this.syncData);
        }
    }

}
