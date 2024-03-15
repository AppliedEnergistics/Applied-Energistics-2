package appeng.items.storage;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.items.AEBaseItem;
import appeng.items.contents.StorageCellCreativeMenuHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.InteractionUtil;

public abstract class StorageCellItem extends AEBaseItem implements IMenuItem {
    public StorageCellItem(Properties properties) {
        super(properties);
    }

    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubmenu) {
        var is = locator.locateItem(player);
        if (is.getItem() == this) {
            return MenuOpener.open(MEStorageMenu.TYPE, player, locator, returningFromSubmenu);
        } else {
            return false;
        }
    }

    @Override
    public @Nullable ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator,
            @Nullable BlockHitResult hitResult) {
        if (player.isCreative()) {
            return new StorageCellCreativeMenuHost<>(this, player, locator,
                    (ignoredPlayer, subMenu) -> this.openFromInventory(player, locator, true));
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        // Allow opening a terminal for any cell in hand in creative mode
        if (player.isCreative() && !InteractionUtil.isInAlternateUseMode(player)) {
            if (!level.isClientSide()) {
                openFromInventory(player, MenuLocators.forHand(player, usedHand), false);
            }
            return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                    player.getItemInHand(usedHand));
        }

        return super.use(level, player, usedHand);
    }
}
