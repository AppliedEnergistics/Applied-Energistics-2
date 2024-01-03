
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.core.network.ClientboundPacket;
import appeng.menu.me.networktool.NetworkStatus;

public record NetworkStatusPacket(NetworkStatus status) implements ClientboundPacket {

    public static NetworkStatusPacket decode(FriendlyByteBuf data) {
        var status = NetworkStatus.read(data);
        return new NetworkStatusPacket(status);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        status.write(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        final Screen gs = Minecraft.getInstance().screen;

        if (gs instanceof NetworkStatusScreen) {
            ((NetworkStatusScreen) gs).processServerUpdate(status);
        }
    }

}
