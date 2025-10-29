package appeng.integration.modules.emi;

import java.util.List;

import net.minecraft.core.NonNullList;
import dev.emi.emi.api.EmiStackPuller;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.menu.me.common.MEStorageMenu;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.PullItemToPlayerPacket;

public class EmiAeBaseScreenStackPuller implements EmiStackPuller<AbstractContainerMenu> {

    @Override
    public boolean pullStack(AbstractContainerMenu menu, List<ItemStack> stacks, long toPull) {
        if (!(menu instanceof MEStorageMenu aeMenu)) {
            return false;
        }

        NonNullList<ItemStack> nonNullStacks = NonNullList.copyOf(stacks);

        ServerboundPacket message = new PullItemToPlayerPacket(menu.containerId, nonNullStacks, toPull);
        PacketDistributor.sendToServer(message);

        return true;
    }

}
