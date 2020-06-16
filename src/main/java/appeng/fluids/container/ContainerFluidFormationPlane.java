
package appeng.fluids.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerHelper;
import appeng.container.slot.SlotRestrictedInput;
import appeng.fluids.parts.PartFluidFormationPlane;
import appeng.fluids.util.IAEFluidTank;

public class ContainerFluidFormationPlane extends ContainerFluidConfigurable {

    public static ContainerType<ContainerFluidFormationPlane> TYPE;

    private static final ContainerHelper<ContainerFluidFormationPlane, PartFluidFormationPlane> helper = new ContainerHelper<>(
            ContainerFluidFormationPlane::new, PartFluidFormationPlane.class, SecurityPermissions.BUILD);

    public static ContainerFluidFormationPlane fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final PartFluidFormationPlane plane;

    public ContainerFluidFormationPlane(int id, final PlayerInventory ip, final PartFluidFormationPlane te) {
        super(TYPE, id, ip, te);
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
        this.addSlot((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187,
                8 + 18 * 2, this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187,
                8 + 18 * 3, this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187,
                8 + 18 * 4, this.getPlayerInventory())).setNotDraggable());
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

            if (y >= upgrades + 2) {
                return false;
            }
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
