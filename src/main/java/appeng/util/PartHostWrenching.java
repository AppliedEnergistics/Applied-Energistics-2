package appeng.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.parts.SelectedPart;

/**
 * Support functionality for using a wrench on parts attached to a part host.
 */
public final class PartHostWrenching {

    private PartHostWrenching() {
    }

    public static void wrenchPart(World world, BlockPos pos, IPartHost host, SelectedPart sp) {
        final List<ItemStack> is = new ArrayList<>();

        if (sp.part != null) {
            is.add(sp.part.getItemStack(PartItemStack.WRENCH));
            sp.part.getDrops(is, true);
            host.removePart(sp.side, false);
        }

        if (sp.facade != null) {
            is.add(sp.facade.getItemStack());
            host.getFacadeContainer().removeFacade(host, sp.side);
            Platform.notifyBlocksOfNeighbors(world, pos);
        }

        if (host.isEmpty()) {
            host.cleanup();
        }

        if (!is.isEmpty()) {
            Platform.spawnDrops(world, pos, is);
        }
    }

}
