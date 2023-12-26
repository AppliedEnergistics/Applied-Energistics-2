package appeng.integration.modules.emi.transfer;

import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.AEBaseMenu;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractRecipeHandler<T extends AEBaseMenu> implements EmiRecipeHandler<T> {
    protected static final int CRAFTING_GRID_WIDTH = 3;
    protected static final int CRAFTING_GRID_HEIGHT = 3;

    private final Class<T> containerClass;

    AbstractRecipeHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
        return new EmiPlayerInventory(List.of());
    }

    protected abstract Result transferRecipe(T menu,
                                             @Nullable RecipeHolder<?> holder,
                                             EmiRecipe emiRecipe,
                                             boolean doTransfer);

    protected final Result transferRecipe(EmiRecipe emiRecipe, EmiCraftContext<T> context, boolean doTransfer) {
        if (!containerClass.isInstance(context.getScreenHandler())) {
            return Result.createNotApplicable();
        }

        T menu = containerClass.cast(context.getScreenHandler());

        var holder = getRecipeHolder(context.getScreenHandler().getPlayer().level(), emiRecipe);

        return transferRecipe(menu, holder, emiRecipe, doTransfer);
    }


    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
        return transferRecipe(recipe, context, false) instanceof Result.Success;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        return transferRecipe(recipe, context, true) instanceof Result.Success;
    }

    @Nullable
    private RecipeHolder<?> getRecipeHolder(Level level, EmiRecipe recipe) {
        if (recipe.getId() != null) {
            // TODO: This can produce false positives...
            return level.getRecipeManager().byKey(recipe.getId()).orElse(null);
        }
        return null;
    }

    protected final boolean isCraftingRecipe(Recipe<?> recipe, EmiRecipe emiRecipe) {
        return EncodingHelper.isSupportedCraftingRecipe(recipe)
                || emiRecipe.getCategory().equals(VanillaEmiRecipeCategories.CRAFTING);
    }

    protected final boolean fitsIn3x3Grid(Recipe<?> recipe, EmiRecipe emiRecipe) {
        if (recipe != null) {
            return recipe.canCraftInDimensions(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT);
        } else {
            return true;
        }
    }

    protected static sealed abstract class Result {
        static final class Success extends Result {
        }

        static final class NotApplicable extends Result {
        }

        static final class Error extends Result {
            private final Component message;

            public Error(Component message) {
                this.message = message;
            }

            public Component getMessage() {
                return message;
            }
        }

        static NotApplicable createNotApplicable() {
            return new NotApplicable();
        }

        static Success createSuccessful() {
            return new Success();
        }

        static Error createFailed(Component text) {
            return new Error(text);
        }
    }
}
