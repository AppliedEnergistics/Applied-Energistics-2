package appeng.items.tools.powered;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.menu.locator.MenuLocators;

public abstract class PoweredContainerItem extends AEBasePoweredItem implements IMenuItem {
    public PoweredContainerItem(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    protected long insert(Player player, ItemStack stack, AEKey what, @Nullable AEKeyType allowed, long amount,
            Actionable mode) {
        if (allowed != null && what.getType() != allowed) {
            return 0;
        }

        var host = getMenuHost(player, MenuLocators.forStack(stack), null);
        if (host == null) {
            return 0;
        }

        return host.insert(player, what, amount, mode);
    }

    // Allow "hovering" up the content of container items in the inventory by right-clicking them
    // with a compatible portable cell.
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        var other = slot.getItem();
        if (other.isEmpty()) {
            return true;
        }

        tryInsertFromPlayerOwnedItem(player, stack, other);
        return true;
    }

    /**
     * Allows directly inserting items and fluids into portable cells by right-clicking the cell with the item or bucket
     * in hand.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        if (other.isEmpty()) {
            return false;
        }

        tryInsertFromPlayerOwnedItem(player, stack, other);
        return true;
    }

    protected boolean tryInsertFromPlayerOwnedItem(Player player,
            ItemStack cellStack,
            ItemStack otherStack) {
        // Try all available strategies
        for (var keyType : ContainerItemStrategies.getSupportedKeyTypes()) {
            if (tryInsertFromPlayerOwnedItem(player, cellStack, otherStack, keyType)) {
                return true;
            }
        }

        // Fall back to inserting as item
        var key = AEItemKey.of(otherStack);
        var inserted = (int) insert(player,
                cellStack,
                key,
                AEKeyType.items(),
                otherStack.getCount(),
                Actionable.MODULATE);
        if (inserted > 0) {
            otherStack.shrink(inserted);
            return true;
        }
        return false;
    }

    protected boolean tryInsertFromPlayerOwnedItem(Player player,
            ItemStack cellStack,
            ItemStack otherStack,
            AEKeyType keyType) {
        var context = ContainerItemStrategies.findOwnedItemContext(keyType, player, otherStack);
        if (context != null) {
            var containedStack = context.getExtractableContent();
            if (containedStack != null) {
                if (insert(player, cellStack, containedStack.what(), keyType, containedStack.amount(),
                        Actionable.SIMULATE) == containedStack.amount()) {
                    var extracted = context.extract(containedStack.what(), containedStack.amount(),
                            Actionable.MODULATE);
                    if (extracted > 0) {
                        insert(player, cellStack, containedStack.what(), keyType, extracted, Actionable.MODULATE);
                        context.playEmptySound(player, containedStack.what());
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
