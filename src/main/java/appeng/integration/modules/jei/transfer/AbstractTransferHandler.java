package appeng.integration.modules.jei.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.crafting.Recipe;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerErrorRenderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.menu.AEBaseMenu;

public abstract class AbstractTransferHandler<T extends AEBaseMenu> implements TransferHandler {

    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    private static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;

    private final Class<T> containerClass;

    AbstractTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public final Result handle(Context context) {
        if (!containerClass.isInstance(context.getMenu())) {
            return Result.createNotApplicable();
        }

        var display = context.getDisplay();

        var craftingRecipe = isCraftingRecipe(display);
        if (isOnlyCraftingSupported() && !craftingRecipe) {
            return Result.createNotApplicable();
        }

        T menu = containerClass.cast(context.getMenu());

        var recipe = getRecipe(display);

        if (display instanceof SimpleGridMenuDisplay gridDisplay) {
            if (gridDisplay.getWidth() > CRAFTING_GRID_WIDTH || gridDisplay.getHeight() > CRAFTING_GRID_HEIGHT) {
                return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
            }
        } else if (display.getInputEntries().size() > CRAFTING_GRID_SLOTS) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
        }

        // Transform ingredients into grid matching the crafting table, we'll do the same for the recipe
        var genericIngredients = getGenericIngredients(display);
        var genericResults = getGenericResults(display);

        // Display information about missing items to craft the recipe
        if (!context.isActuallyCrafting()) {
            var missingSlots = findMissingSlots(menu, recipe, genericIngredients);

            if (!missingSlots.isEmpty()) {
                // TODO: Currently REI has no "warning" state and it's either all or nothing. Error disables
                // transfer, which we don't want, and success doesn't call the renderer.
                return Result.createSuccessful()
                        .color(0x80FFA500)
                        .errorRenderer(new MissingSlots(missingSlots));
            }
        } else {
            // The user clicked the button to actually perform the transfer
            performTransfer(menu, recipe, genericIngredients, genericResults, craftingRecipe);
        }

        return Result.createSuccessful().blocksFurtherHandling();
    }

    /**
     * Is the recipe to be shown a recipe that can be crafted in a Vanilla crafting table? We equate this with Vanilla
     * {@link net.minecraft.world.item.crafting.RecipeType#CRAFTING}.
     */
    private boolean isCraftingRecipe(Display display) {
        return Objects.equals(
                display.getCategoryIdentifier(),
                CategoryIdentifier.of("minecraft", "plugins/crafting"));
    }

    protected abstract boolean isOnlyCraftingSupported();

    protected abstract void performTransfer(T menu,
            @Nullable Recipe<?> recipe,
            List<List<GenericStack>> genericIngredients,
            List<GenericStack> genericResults, boolean forCraftingTable);

    @Nullable
    private Recipe<?> getRecipe(Display display) {
        // Displays can be based on completely custom objects, or on actual Vanilla recipes
        var origin = DisplayRegistry.getInstance().getDisplayOrigin(display);

        return origin instanceof Recipe<?>recipe ? recipe : null;
    }

    protected Set<Integer> findMissingSlots(T menu,
            @Nullable Recipe<?> recipe,
            List<List<GenericStack>> genericIngredients) {
        return Collections.emptySet();
    }

    private List<List<GenericStack>> getGenericIngredients(Display display) {
        return get3x3Ingredients(display).stream()
                .map(slotIngredients -> slotIngredients.stream()
                        .map(GenericEntryStackHelper::of)
                        .filter(Objects::nonNull)
                        .toList())
                .toList();
    }

    private List<GenericStack> getGenericResults(Display display) {
        return display.getOutputEntries().stream()
                .map(ei -> {
                    // Use the first convertible entry
                    for (var entryStack : ei) {
                        var genericStack = GenericEntryStackHelper.of(entryStack);
                        if (genericStack != null) {
                            return genericStack;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<EntryIngredient> get3x3Ingredients(Display display) {
        // For recipes smaller than 3x3, the displays' ingredient may be less than 3x3
        var inputs3x3 = new ArrayList<EntryIngredient>(CRAFTING_GRID_SLOTS);
        for (int i = 0; i < CRAFTING_GRID_SLOTS; i++) {
            inputs3x3.add(EntryIngredient.empty());
        }

        if (display instanceof SimpleGridMenuDisplay gridDisplay) {
            for (int i = 0; i < display.getInputEntries().size(); i++) {
                var entry = display.getInputEntries().get(i);
                if (!entry.isEmpty()) {
                    inputs3x3.set(getSlotWithSize(gridDisplay.getWidth(), i), entry);
                }
            }
        } else {
            // Don't assume any specific slot layout, but ensure it's padded up to 9 slots anyway
            for (int i = 0; i < inputs3x3.size(); i++) {
                if (i < display.getInputEntries().size()) {
                    inputs3x3.set(i, display.getInputEntries().get(i));
                }
            }
        }

        return inputs3x3;
    }

    /**
     * Draw missing slots.
     */
    @Override
    public @Nullable TransferHandlerErrorRenderer provideErrorRenderer(Context context, Object data) {
        if (!(data instanceof MissingSlots missingSlots)) {
            return null;
        }

        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int i = 0;
            for (Widget widget : widgets) {
                if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
                    if (missingSlots.indices().contains(i++)) {
                        matrices.pushPose();
                        matrices.translate(0, 0, 400);
                        Rectangle innerBounds = ((Slot) widget).getInnerBounds();
                        GuiComponent.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(),
                                innerBounds.getMaxY(), 0x40ff0000);
                        matrices.popPose();
                    }
                }
            }
        };
    }

    private static int getSlotWithSize(int recipeWidth, int index) {
        int x = index % recipeWidth;
        int y = (index - x) / recipeWidth;
        return CRAFTING_GRID_WIDTH * y + x;
    }

    private record MissingSlots(Set<Integer> indices) {
    }
}
