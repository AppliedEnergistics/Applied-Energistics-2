package appeng.menu.implementations;

import java.util.Map;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.helpers.DualityItemInterface;
import appeng.helpers.iface.DualityCraftingInterface;
import appeng.helpers.iface.GenericStackInv;
import appeng.helpers.iface.GenericStackSyncHelper;
import appeng.helpers.iface.ICraftingInterfaceHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.RestrictedInputSlot;

public class CraftingInterfaceMenu extends AEBaseMenu implements IGenericSyncMenu {

    public static final MenuType<CraftingInterfaceMenu> TYPE = MenuTypeBuilder
            .create(CraftingInterfaceMenu::new, ICraftingInterfaceHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("crafting_interface");

    private final DualityCraftingInterface duality;
    private final GenericStackSyncHelper syncHelper;

    @GuiSync(3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(4)
    public YesNo showInInterfaceTerminal = YesNo.YES;

    public CraftingInterfaceMenu(int id, Inventory playerInventory, ICraftingInterfaceHost host) {
        super(TYPE, id, playerInventory, host);

        this.createPlayerInventorySlots(playerInventory);

        this.duality = host.getDuality();
        this.syncHelper = new GenericStackSyncHelper(getReturnInv(), 0);

        for (int x = 0; x < DualityItemInterface.NUMBER_OF_PATTERN_SLOTS; x++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                    duality.getPatternInv(), x),
                    SlotSemantic.ENCODED_PATTERN);
        }
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            blockingMode = duality.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInInterfaceTerminal = duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL);

            this.syncHelper.sendDiff(getPlayer());
        }

        super.broadcastChanges();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        this.syncHelper.sendFull(getPlayer());
    }

    @Override
    public void receiveGenericStacks(Map<Integer, IAEStack> stacks) {
        if (isClient()) {
            for (var entry : stacks.entrySet()) {
                getReturnInv().setStack(entry.getKey(), entry.getValue());
            }
        } else {
            AELog.warn("Client tried to override crafting interface return stacks!");
        }
    }

    public GenericStackInv getReturnInv() {
        return duality.getReturnInv();
    }

    public YesNo getBlockingMode() {
        return blockingMode;
    }

    public YesNo getShowInInterfaceTerminal() {
        return showInInterfaceTerminal;
    }
}
