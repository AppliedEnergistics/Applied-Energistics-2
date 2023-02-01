package appeng.core.sync.packets;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.powered.Terminal;
import appeng.util.Platform;
import baubles.api.BaublesApi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PacketTerminalUse extends AppEngPacket {
    Terminal terminal;

    public PacketTerminalUse(final ByteBuf stream) {
        this.terminal = Terminal.values()[stream.readInt()];
    }

    public PacketTerminalUse(Enum<Terminal> terminal) {

        final ByteBuf data = Unpooled.buffer();

        data.writeInt(this.getPacketID());
        data.writeInt(terminal.ordinal());

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player) {
        NonNullList<ItemStack> mainInventory = player.inventory.mainInventory;
        for (int i = 0; i < mainInventory.size(); i++) {
            ItemStack is = mainInventory.get(i);
            if (is.isItemEqual(terminal.getItemDefinition().maybeStack(1).get())) {
                Platform.openGUI(player, i, terminal.getBridge(), false);
                return;
            }
        }
        for (int i = 0; i < BaublesApi.getBaublesHandler(player).getSlots(); i++) {
            ItemStack is = BaublesApi.getBaublesHandler(player).getStackInSlot(i);
            if (is.isItemEqual(terminal.getItemDefinition().maybeStack(1).get())) {
                Platform.openGUI(player, i, terminal.getBridge(), true);
                break;
            }
        }
    }

    enum a {

    }
}
