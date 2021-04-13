package appeng.container.implementations;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.IUpgradeableHost;
import appeng.container.ContainerLocator;
import appeng.container.SlotSemantic;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.OptionalTypeOnlyFakeSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

/**
 * Used for {@link appeng.parts.automation.ImportBusPart} and {@link appeng.parts.automation.ExportBusPart}
 */
public class IOBusContainer extends UpgradeableContainer {

    public static ContainerType<IOBusContainer> TYPE;

    private static final ContainerHelper<IOBusContainer, IUpgradeableHost> helper = new ContainerHelper<>(
            IOBusContainer::new, IUpgradeableHost.class, SecurityPermissions.BUILD);

    public static IOBusContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

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
