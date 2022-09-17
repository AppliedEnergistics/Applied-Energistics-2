package appeng.fluids.container;


import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.slot.SlotRestrictedInput;
import appeng.fluids.parts.PartFluidFormationPlane;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;


public class ContainerFluidFormationPlane extends ContainerFluidConfigurable {
    private final PartFluidFormationPlane plane;

    public ContainerFluidFormationPlane(final InventoryPlayer ip, final PartFluidFormationPlane te) {
        super(ip, te);
        this.plane = te;
    }

    @Override
    protected int getHeight() {
        return 251;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.plane.getConfig();
    }

    @Override
    protected void setupConfig() {
        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer()))
                .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, this.getInventoryPlayer()))
                        .setNotDraggable());
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        this.checkToolbox();
        this.standardDetectAndSendChanges();
    }

    @Override
    protected boolean isValidForConfig(int slot, IAEFluidStack fs) {
        if (this.supportCapacity()) {
            final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

            final int y = slot / 9;

            return y < upgrades + 2;
        }

        return true;
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

        return upgrades > idx;
    }

    @Override
    protected boolean supportCapacity() {
        return true;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }
}
