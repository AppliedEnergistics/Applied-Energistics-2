package appeng.container.implementations;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.IUpgradeableHost;
import appeng.container.SlotSemantic;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.OptionalTypeOnlyFakeSlot;

/**
 * Used for {@link appeng.parts.automation.ImportBusPart} and {@link appeng.parts.automation.ExportBusPart}
 */
public class IOBusContainer extends UpgradeableContainer {

    public static final ContainerType<IOBusContainer> TYPE = ContainerTypeBuilder
            .create(IOBusContainer::new, IUpgradeableHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("iobus");

    public IOBusContainer(int id, final PlayerInventory ip, final IUpgradeableHost te) {
        super(TYPE, id, ip, te);
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");
        final SlotSemantic s = SlotSemantic.CONFIG;

        this.addSlot(new FakeTypeOnlySlot(inv, 0), s);

        // Slots that become available with 1 capacity card
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 1, 1), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 2, 1), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 3, 1), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 4, 1), s);

        // Slots that become available with 2 capacity cards
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 5, 2), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 6, 2), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 7, 2), s);
        this.addSlot(new OptionalTypeOnlyFakeSlot(inv, this, 8, 2), s);
    }

}
