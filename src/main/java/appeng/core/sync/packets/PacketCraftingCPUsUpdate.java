package appeng.core.sync.packets;

import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.container.implementations.CraftingCPUStatus;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.Collection;

public class PacketCraftingCPUsUpdate extends AppEngPacket {

    private final CraftingCPUStatus[] cpus;

    public PacketCraftingCPUsUpdate(final ByteBuf stream) {
        int count = stream.readInt();
        cpus = new CraftingCPUStatus[count];
        for (int i = 0; i < count; i++) {
            try {
                cpus[i] = new CraftingCPUStatus(stream);
            } catch (IOException e) {
                cpus[i] = new CraftingCPUStatus();
            }
        }
    }

    public PacketCraftingCPUsUpdate(final Collection<CraftingCPUStatus> cpus) throws IOException {
        this.cpus = cpus.toArray(new CraftingCPUStatus[0]);

        final ByteBuf data = Unpooled.buffer();
        data.writeInt(this.getPacketID());
        data.writeInt(this.cpus.length);
        for (CraftingCPUStatus cpu : this.cpus) {
            cpu.writeToPacket(data);
        }
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

        if (gs instanceof GuiCraftingStatus) {
            GuiCraftingStatus gui = (GuiCraftingStatus) gs;
            gui.postCPUUpdate(this.cpus);
        }

    }
}
