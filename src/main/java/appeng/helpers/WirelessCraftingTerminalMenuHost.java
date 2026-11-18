package appeng.helpers;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import appeng.api.ids.AEComponents;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.items.contents.AccessDependentSupplier;
import appeng.items.contents.ItemAccessHelper;
import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.SupplierInternalInventory;

public class WirelessCraftingTerminalMenuHost<T extends WirelessCraftingTerminalItem>
        extends WirelessTerminalMenuHost<T> implements ISegmentedInventory {
    private final SupplierInternalInventory<InternalInventory> craftingGrid;

    public WirelessCraftingTerminalMenuHost(T item, Player player, ItemMenuHostLocator locator,
            BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        this.craftingGrid = new SupplierInternalInventory<>(
                new AccessDependentSupplier<>(itemAccess(), access -> createCraftingInv(player, access)));
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(Identifier id) {
        if (id.equals(CraftingTerminalPart.INV_CRAFTING)) {
            return craftingGrid;
        } else {
            return null;
        }
    }

    private static InternalInventory createCraftingInv(Player player, ItemAccess access) {
        var craftingGrid = new AppEngInternalInventory(new InternalInventoryHost() {
            @Override
            public void saveChangedInventory(AppEngInternalInventory inv) {
                ItemAccessHelper.modify(access,
                        resource -> resource.with(AEComponents.CRAFTING_INV, inv.toItemContainerContents()));
            }

            @Override
            public boolean isClientSide() {
                return player.level().isClientSide();
            }
        }, 9);
        craftingGrid
                .fromItemContainerContents(
                        access.getResource().getOrDefault(AEComponents.CRAFTING_INV, ItemContainerContents.EMPTY));
        return craftingGrid;
    }
}
