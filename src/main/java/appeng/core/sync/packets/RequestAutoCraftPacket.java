package appeng.core.sync.packets;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.crafting.CraftAmountContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

/**
 * This packet opens the {@link appeng.client.gui.me.crafting.CraftAmountScreen} to initiate
 * auto-crafting while a {@link appeng.client.gui.me.items.MEMonitorableScreen} is open.
 */
public class RequestAutoCraftPacket extends BasePacket {

    // The item to craft
    private IAEItemStack stack;

    public RequestAutoCraftPacket(PacketBuffer buffer) {
        stack = AEItemStack.fromPacket(buffer);
    }

    /**
     * @param stack The item to request auto crafting for.
     */
    public RequestAutoCraftPacket(IAEItemStack stack) {
        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        stack.writeToPacket(data);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final ServerPlayerEntity sender = (ServerPlayerEntity) player;
        if (sender.openContainer instanceof AEBaseContainer) {
            final AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
            final ContainerLocator locator = baseContainer.getLocator();
            if (locator != null) {
                ContainerOpener.openContainer(CraftAmountContainer.TYPE, player, locator);

                if (sender.openContainer instanceof CraftAmountContainer) {
                    final CraftAmountContainer cca = (CraftAmountContainer) sender.openContainer;

                    // This will be sent to the client to indicate what is about to be crafted
                    cca.getCraftingItem().putStack(stack.asItemStackRepresentation());
                    // This is the *actual* item that matters, not the display item above
                    cca.setItemToCraft(stack);

                    cca.detectAndSendChanges();
                }
            }
        }
    }

}
