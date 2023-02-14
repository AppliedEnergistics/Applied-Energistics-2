package appeng.core.sync.packets;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.core.localization.PlayerMessages;
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
            if (terminal.getItemDefinition().isSameAs(is)) {
                openGui(is, i, player, false);
                return;

            }
        }
        for (int i = 0; i < BaublesApi.getBaublesHandler(player).getSlots(); i++) {
            ItemStack is = BaublesApi.getBaublesHandler(player).getStackInSlot(i);
            if (terminal.getItemDefinition().isSameAs(is)) {
                openGui(is, i, player, true);
                break;
            }
        }
    }

    void openGui(ItemStack itemStack, int slotIdx, EntityPlayer player, boolean isBauble) {
        final IWirelessTermHandler handler = AEApi.instance().registries().wireless().getWirelessTerminalHandler(itemStack);
        if (handler == null) {
            return;
        }

        final String unparsedKey = handler.getEncryptionKey(itemStack);
        if (unparsedKey.isEmpty()) {
            player.sendMessage(PlayerMessages.DeviceNotLinked.get());
            return;
        }

        final long parsedKey = Long.parseLong(unparsedKey);
        final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);
        if (securityStation == null) {
            player.sendMessage(PlayerMessages.StationCanNotBeLocated.get());
            return;
        }

        if (handler.hasPower(player, 0.5, itemStack)) {
            Platform.openGUI(player, slotIdx, terminal.getBridge(), isBauble);
        } else {
            player.sendMessage(PlayerMessages.DeviceNotPowered.get());
        }
    }
}
