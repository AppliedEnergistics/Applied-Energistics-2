package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.container.me.crafting.CraftingStatus;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class CraftingStatusPacket extends BasePacket {
    private final CraftingStatus status;

    public CraftingStatusPacket(PacketBuffer buffer) {
        this.status = CraftingStatus.read(buffer);
    }

    public CraftingStatusPacket(CraftingStatus status) {
        this.status = status;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(getPacketID());
        status.write(data);
        configureWrite(data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        Screen screen = Minecraft.getInstance().currentScreen;

        if (screen instanceof CraftingCPUScreen) {
            CraftingCPUScreen<?> cpuScreen = (CraftingCPUScreen<?>) screen;
            cpuScreen.postUpdate(this.status);
        }
    }

}
