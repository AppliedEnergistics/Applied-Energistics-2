package appeng.client.gui.me.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.common.TerminalStyle;
import appeng.client.gui.widgets.IScrollSource;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.util.prioritylist.IPartitionList;

public class ItemTerminalScreen<C extends MEMonitorableContainer<IAEItemStack>>
        extends MEMonitorableScreen<IAEItemStack, C> {
    public ItemTerminalScreen(TerminalStyle style, C container, PlayerInventory playerInventory, ITextComponent title) {
        super(style, container, playerInventory, title);
    }

    @Override
    protected Repo<IAEItemStack> createRepo(IScrollSource scrollSource) {
        return new ItemRepo(scrollSource, this);
    }

    @Override
    protected IPartitionList<IAEItemStack> createPartitionList(List<ItemStack> viewCells) {
        return ViewCellItem.createFilter(viewCells);
    }

    @Override
    protected void renderGridInventoryEntry(MatrixStack matrices, int x, int y,
            GridInventoryEntry<IAEItemStack> entry) {
        // Annoying but easier than trying to splice into render item
        ItemStack displayStack = entry.getStack().asItemStackRepresentation();
        Inventory displayInv = new Inventory(displayStack);
        super.moveItems(matrices, new Slot(displayInv, 0, x, y));
    }

    @Override
    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<IAEItemStack> entry, int mouseButton,
            ClickType clickType) {
        if (entry == null) {
            // The only interaction allowed on an empty virtual slot is putting down the currently held item
            if (clickType == ClickType.PICKUP && !playerInventory.getItemStack().isEmpty()) {
                InventoryAction action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                        : InventoryAction.PICKUP_OR_SET_DOWN;
                container.handleInteraction(-1, action);
            }
            return;
        }

        long serial = entry.getSerial();

        // Move as many items of a single type as possible
        if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_SPACE)) {
            container.handleInteraction(serial, InventoryAction.MOVE_REGION);
        } else {
            InventoryAction action = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = (mouseButton == 1) ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;

                    if (action == InventoryAction.PICKUP_OR_SET_DOWN
                            && isViewOnlyCraftable()
                            && playerInventory.getItemStack().isEmpty()) {
                        container.handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    }

                    break;
                case QUICK_MOVE:
                    action = (mouseButton == 1) ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case CLONE: // creative dupe:
                    if (entry.isCraftable()) {
                        container.handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    } else if (playerInventory.player.abilities.isCreativeMode) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }
                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                container.handleInteraction(serial, action);
            }
        }
    }

}
