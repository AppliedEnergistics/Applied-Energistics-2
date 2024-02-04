package appeng.core.network.clientbound;

import appeng.api.storage.ILinkStatus;
import appeng.api.storage.LinkStatus;
import appeng.core.network.ClientboundPacket;
import appeng.menu.guisync.LinkStatusAwareMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public record SetLinkStatusPacket(ILinkStatus linkStatus) implements ClientboundPacket {
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(linkStatus.connected());
        buffer.writeOptional(Optional.ofNullable(linkStatus.statusDescription()), FriendlyByteBuf::writeComponent);
    }

    public static SetLinkStatusPacket decode(FriendlyByteBuf buffer) {
        return new SetLinkStatusPacket(new LinkStatus(
                buffer.readBoolean(),
                buffer.readOptional(FriendlyByteBuf::readComponentTrusted).orElse(null)
        ));
    }

    @Override
    public void handleOnClient(Player player) {
        if (player.containerMenu instanceof LinkStatusAwareMenu linkStatusAwareMenu) {
            linkStatusAwareMenu.setLinkStatus(linkStatus);
        }
    }
}
