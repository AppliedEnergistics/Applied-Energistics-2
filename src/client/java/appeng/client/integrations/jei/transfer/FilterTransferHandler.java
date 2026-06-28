package appeng.client.integrations.jei.transfer;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;

import appeng.api.upgrades.IUpgradeableObject;
import appeng.client.integrations.jei.GenericEntryStackHelper;
import appeng.helpers.FilterTransferHelper;
import appeng.menu.implementations.UpgradeableMenu;

public class FilterTransferHandler<T extends UpgradeableMenu<? extends IUpgradeableObject>>
        extends AbstractTransferHandler implements IUniversalRecipeTransferHandler<T> {

    private final Class<T> menuClass;

    public FilterTransferHandler(MenuType<T> menuType, Class<T> menuClass, IRecipeTransferHandlerHelper helper) {

        this.menuClass = menuClass;

    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(T menu, Object recipeBase, IRecipeSlotsView slotsView, Player player,
            boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            var recipeInputs = GenericEntryStackHelper.ofInputs(slotsView);
            FilterTransferHelper<T> helper = new FilterTransferHelper<>();
            helper.transfer(menu, recipeInputs);
        }
        return null;
    }

    // Returning empty means the handler will not limit itself to a single MenuType from any given container class.
    @Override
    public Optional<MenuType<T>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public Class<? extends T> getContainerClass() {
        return menuClass;
    }
}
