
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.core.network.ClientboundPacket;
import appeng.menu.me.crafting.CraftingStatus;

public record CraftingStatusPacket(CraftingStatus status) implements ClientboundPacket {
    public static CraftingStatusPacket decode(FriendlyByteBuf buffer) {
        return new CraftingStatusPacket(CraftingStatus.read(buffer));
    }

    @Override
    public void write(FriendlyByteBuf data) {
        status.write(data);
    }

    @Override
    public void handleOnClient(Player player) {
        Screen screen = Minecraft.getInstance().screen;

        if (screen instanceof CraftingCPUScreen<?>cpuScreen) {
            cpuScreen.postUpdate(this.status);
        }
    }

}
