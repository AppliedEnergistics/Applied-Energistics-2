package appeng.integration.modules.rei.transfer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;

import appeng.menu.AEBaseMenu;

public abstract class AbstractTransferHandler<T extends AEBaseMenu> implements TransferHandler {
    protected static final int CRAFTING_GRID_WIDTH = 3;
    protected static final int CRAFTING_GRID_HEIGHT = 3;
    private static final CategoryIdentifier<?> CRAFTING = CategoryIdentifier.of("minecraft", "plugins/crafting");

    private final Class<T> containerClass;

    AbstractTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    protected abstract Result transferRecipe(T menu,
            @Nullable Recipe<?> recipe,
            Display display,
            boolean doTransfer);

    @Override
    public final Result handle(Context context) {
        if (!containerClass.isInstance(context.getMenu())) {
            return Result.createNotApplicable();
        }

        var display = context.getDisplay();

        T menu = containerClass.cast(context.getMenu());

        var recipe = getRecipe(display);

        return transferRecipe(menu, recipe, display, context.isActuallyCrafting());
    }

    @Nullable
    private Recipe<?> getRecipe(Display display) {
        // Displays can be based on completely custom objects, or on actual Vanilla recipes
        var origin = DisplayRegistry.getInstance().getDisplayOrigin(display);

        return origin instanceof Recipe<?>recipe ? recipe : null;
    }

    protected final boolean isCraftingRecipe(Recipe<?> recipe, Display display) {
        return recipe != null && recipe.getType() == RecipeType.CRAFTING
                || display.getCategoryIdentifier().equals(CRAFTING);
    }

    protected final boolean fitsIn3x3Grid(Recipe<?> recipe, Display display) {
        if (recipe != null) {
            return recipe.canCraftInDimensions(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT);
        } else if (display instanceof SimpleGridMenuDisplay gridDisplay) {
            return gridDisplay.getWidth() <= CRAFTING_GRID_WIDTH && gridDisplay.getHeight() <= CRAFTING_GRID_HEIGHT;
        } else {
            return true;
        }
    }
}
