package appeng.fluids.container;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.UpgradeableContainer;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;

public abstract class FluidConfigurableContainer extends UpgradeableContainer implements IFluidSyncContainer {
    private FluidSyncHelper sync = null;

    public FluidConfigurableContainer(ScreenHandlerType<?> containerType, int id, PlayerInventory ip,
            IUpgradeableHost te) {
        super(containerType, id, ip, te);
    }

    public abstract IAEFluidTank getFluidConfigInventory();

    private FluidSyncHelper getSyncHelper() {
        if (this.sync == null) {
            this.sync = new FluidSyncHelper(this.getFluidConfigInventory(), 0);
        }
        return this.sync;
    }

    @Override
    protected ItemStack transferStackToContainer(ItemStack input) {
        FluidVolume fluid = FluidAttributes.EXTRACTABLE.get(input).attemptAnyExtraction(FluidAmount.MAX_VALUE,
                Simulation.ACTION);

        if (!fluid.isEmpty()) {
            final IAEFluidTank t = this.getFluidConfigInventory();
            final IAEFluidStack stack = AEFluidStack.fromFluidVolume(fluid, RoundingMode.DOWN);
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
            if (slot > 4 && upgrades < 2) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServer()) {
            this.getSyncHelper().sendDiff(this.getListeners());

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
    public void addListener(ScreenHandlerListener listener) {
        super.addListener(listener);
        this.getSyncHelper().sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        this.getSyncHelper().readPacket(fluids);
    }

}
