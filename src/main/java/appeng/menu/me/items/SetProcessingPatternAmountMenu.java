package appeng.menu.me.items;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.IPatternTerminalHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.InaccessibleSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.util.inv.AppEngInternalInventory;

/**
 * Allows precisely setting the amount for a slot in a processing pattern.
 *
 * @see appeng.client.gui.me.items.SetProcessingPatternAmountScreen
 */
public class SetProcessingPatternAmountMenu extends AEBaseMenu implements ISubMenu {

    public static final MenuType<SetProcessingPatternAmountMenu> TYPE = MenuTypeBuilder
            .create(SetProcessingPatternAmountMenu::new, IPatternTerminalHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("set_processing_pattern_amount");

    public static final String ACTION_SET_AMOUNT = "setAmount";

    /**
     * This slot is used to synchronize a visual representation of the pattern slot we're setting the amount for.
     */
    private final Slot visualSlot;

    @GuiSync(1)
    private int initialAmount = -1;

    @GuiSync(2)
    private int maxAmount = -1;

    private InternalInventory slotInv;

    private final IPatternTerminalHost host;

    public SetProcessingPatternAmountMenu(int id, Inventory ip, IPatternTerminalHost host) {
        super(TYPE, id, ip, host);
        registerClientAction(ACTION_SET_AMOUNT, Integer.class, this::confirm);
        this.host = host;
        this.visualSlot = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.addSlot(this.visualSlot, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public IPatternTerminalHost getHost() {
        return host;
    }

    /**
     * Opens the screen to enter the amount for a slot within a sub-inventory of the pattern encoder.
     */
    public static void open(ServerPlayer player, MenuLocator locator, InternalInventory slotInv) {
        MenuOpener.open(SetProcessingPatternAmountMenu.TYPE, player, locator);

        if (player.containerMenu instanceof SetProcessingPatternAmountMenu menu) {
            menu.setPatternSlot(slotInv);
            menu.broadcastChanges();
        }
    }

    @Override
    public void broadcastChanges() {
        if (host.getMode() != EncodingMode.PROCESSING) {
            host.returnToMainMenu(getPlayer(), this);
            return;
        }

        super.broadcastChanges();
        this.verifyPermissions(SecurityPermissions.BUILD, false);
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level;
    }

    private void setPatternSlot(InternalInventory slotInv) {
        this.slotInv = slotInv;
        var stack = slotInv.getStackInSlot(0);
        var wrappedStack = GenericStack.unwrapItemStack(stack);
        if (wrappedStack != null) {
            this.initialAmount = Ints.saturatedCast(wrappedStack.amount());
        } else {
            this.initialAmount = stack.getCount();
        }
        this.maxAmount = Integer.MAX_VALUE;
        this.visualSlot.set(stack);
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Changes the amount to be stocked.
     *
     * @param amount The number of items to stock.
     */
    public void confirm(int amount) {
        if (isClientSide()) {
            sendClientAction(ACTION_SET_AMOUNT, amount);
            return;
        }

        var currentStack = slotInv.getStackInSlot(0);

        // In case the config changed don't set anything
        if (!Objects.equals(currentStack, visualSlot.getItem())) {
            host.returnToMainMenu(getPlayer(), this);
            return;
        }

        amount = Mth.clamp(amount, 0, Integer.MAX_VALUE);

        if (amount <= 0) {
            slotInv.setItemDirect(0, ItemStack.EMPTY);
        } else {
            var unwrapped = GenericStack.fromItemStack(currentStack);
            if (unwrapped != null) {
                slotInv.setItemDirect(
                        0,
                        GenericStack.wrapInItemStack(unwrapped.what(), amount));
            }
        }
        host.returnToMainMenu(getPlayer(), this);
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    @Nullable
    public AEKey getWhatToStock() {
        var stack = GenericStack.fromItemStack(visualSlot.getItem());
        return stack != null ? stack.what() : null;
    }
}
