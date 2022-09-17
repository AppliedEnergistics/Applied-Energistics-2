package appeng.fluids.container;


import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Collections;
import java.util.Map;


public abstract class ContainerFluidConfigurable extends ContainerUpgradeable implements IFluidSyncContainer {
    private FluidSyncHelper sync = null;

    public ContainerFluidConfigurable(InventoryPlayer ip, IUpgradeableHost te) {
        super(ip, te);
    }

    public abstract IAEFluidTank getFluidConfigInventory();

    private FluidSyncHelper getSynchHelper() {
        if (this.sync == null) {
            this.sync = new FluidSyncHelper(this.getFluidConfigInventory(), 0);
        }
        return this.sync;
    }

    @Override
    protected ItemStack transferStackToContainer(ItemStack input) {
        FluidStack fs = FluidUtil.getFluidContained(input);
        if (fs != null) {
            final IAEFluidTank t = this.getFluidConfigInventory();
            final IAEFluidStack stack = AEFluidStack.fromFluidStack(fs);
            for (int i = 0; i < t.getSlots(); ++i) {
                if (t.getFluidInSlot(i) == null && this.isValidForConfig(i, stack)) {
                    t.setFluidInSlot(i, stack);
                    break;
                }
            }
        }
        return input;
    }

    protected boolean isValidForConfig(int slot, IAEFluidStack fs) {
        if (this.supportCapacity()) {
            // assumes 4 slots per upgrade
            final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

            if (slot > 0 && upgrades < 1) {
                return false;
            }
            return slot <= 4 || upgrades >= 2;
        }

        return true;
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (Platform.isServer()) {
            this.getSynchHelper().sendDiff(this.listeners);

            // clear out config items that are no longer valid (eg capacity upgrade removed)
            final IAEFluidTank t = this.getFluidConfigInventory();
            for (int i = 0; i < t.getSlots(); ++i) {
                if (t.getFluidInSlot(i) != null && !this.isValidForConfig(i, t.getFluidInSlot(i))) {
                    t.setFluidInSlot(i, null);
                }
            }
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.getSynchHelper().sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        this.getSynchHelper().readPacket(fluids);
    }

}
