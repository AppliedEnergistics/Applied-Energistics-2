package appeng.integration.modules.emi;

import java.util.List;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PullItemToPlayerPacket;
import appeng.menu.me.common.MEStorageMenu;
import dev.emi.emi.api.EmiStackPuller;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

public class EmiAeBaseScreenStackPuller implements EmiStackPuller<AbstractContainerMenu> {

    @Override
    public boolean pullStack(AbstractContainerMenu menu, List<ItemStack> stacks, long toPull) {
        if (!(menu instanceof MEStorageMenu aeMenu)) {
            return false;
        }

        NonNullList<ItemStack> nonNullStacks = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            nonNullStacks.set(i, stacks.get(i));
        }

        NetworkHandler.instance()
            .sendToServer(new PullItemToPlayerPacket(menu.containerId, nonNullStacks, toPull));

        return true;
    }

}
