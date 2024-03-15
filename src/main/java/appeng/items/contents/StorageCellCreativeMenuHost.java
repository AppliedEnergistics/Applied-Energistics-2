package appeng.items.contents;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.SupplierStorage;
import appeng.api.util.IConfigManager;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.ConfigManager;

/**
 * Menu host for inspecting and modifying the contents of any storage cell without a terminal or ME chest.
 */
public class StorageCellCreativeMenuHost<T extends Item> extends ItemMenuHost<T>
        implements ITerminalHost, IEnergySource {
    private final BiConsumer<Player, ISubMenu> returnMainMenu;
    private final MEStorage cellStorage;

    public StorageCellCreativeMenuHost(T item, Player player, ItemMenuHostLocator locator,
            BiConsumer<Player, ISubMenu> returnMainMenu) {
        super(item, player, locator);
        Preconditions.checkArgument(StorageCells.isCellHandled(getItemStack()),
                "A creative terminal can only be opened for storage cells.");
        Preconditions.checkArgument(getItemStack().is(item), "Stack doesn't match item");
        this.returnMainMenu = returnMainMenu;
        this.cellStorage = new SupplierStorage(new CellStorageSupplier(this::getItemStack));
        Objects.requireNonNull(cellStorage, "Storage cell doesn't expose a cell inventory.");
    }

    @Override
    public boolean isValid() {
        return getPlayer().isCreative() && super.isValid();
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return ILinkStatus.ofConnected();
    }

    @Override
    public MEStorage getInventory() {
        return cellStorage;
    }

    @Override
    public IConfigManager getConfigManager() {
        return new ConfigManager((manager, settingName) -> {
        });
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        returnMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemStack();
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        if (getPlayer().isCreative()) {
            return amt;
        }
        return 0;
    }
}
