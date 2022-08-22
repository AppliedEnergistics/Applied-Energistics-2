package appeng.integration.modules.rei.transfer;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.integration.modules.rei.GenericEntryStackHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;

/**
 * Handles encoding patterns in the {@link PatternEncodingTermMenu} by clicking the + button on recipes shown in REI (or
 * JEI).
 */
public class EncodePatternTransferHandler<T extends PatternEncodingTermMenu> extends AbstractTransferHandler<T> {

    private static final int BLUE_PLUS_BUTTON_COLOR = 0x804545FF;

    private final IngredientVisibility ingredientVisibility = new IngredientVisibility();

    public EncodePatternTransferHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected Result transferRecipe(T menu, Recipe<?> recipe, Display display, boolean doTransfer) {

        // Crafting recipe slots are not grouped, hence they must fit into the 3x3 grid.
        boolean craftingRecipe = isCraftingRecipe(recipe, display);
        if (craftingRecipe && !fitsIn3x3Grid(recipe, display)) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (craftingRecipe) {
                EncodingHelper.encodeCraftingRecipe(menu,
                        recipe,
                        getGuiIngredientsForCrafting(display),
                        this::isIngredientVisible);
            } else {
                EncodingHelper.encodeProcessingRecipe(menu,
                        GenericEntryStackHelper.ofInputs(display),
                        GenericEntryStackHelper.ofOutputs(display));
            }
        } else {
            Pair<Set<Integer>, Set<Integer>> craftableSlots = findCraftableSlots(menu, display);
            if (!craftableSlots.getLeft().isEmpty() || !craftableSlots.getRight().isEmpty()) {
                return Result.createSuccessful()
                        .color(BLUE_PLUS_BUTTON_COLOR)
                        .renderer(createErrorRenderer(craftableSlots))
                        .overrideTooltipRenderer((point, sink) -> sink.accept(
                                Tooltip.create(Collections.singletonList(
                                        ItemModText.INGREDIENT_CRAFTABLE.text().withStyle(ChatFormatting.BLUE)))));
            }

        }

        return Result.createSuccessful().blocksFurtherHandling();
    }

    private boolean isIngredientVisible(ItemStack itemStack) {
        return ingredientVisibility.isVisible(itemStack);
    }

    /**
     * In case the recipe does not report inputs, we will use the inputs shown on the JEI GUI instead.
     */
    private List<List<GenericStack>> getGuiIngredientsForCrafting(Display recipeLayout) {
        var result = new ArrayList<List<GenericStack>>(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT);
        for (int i = 0; i < CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT; i++) {
            var stacks = new ArrayList<GenericStack>();

            if (i < recipeLayout.getInputEntries().size()) {
                for (EntryStack<?> entryStack : recipeLayout.getInputEntries().get(i)) {
                    if (entryStack.getType() == VanillaEntryTypes.ITEM) {
                        stacks.add(GenericStack.fromItemStack(entryStack.castValue()));
                    }
                }
            }

            result.add(stacks);
        }

        return result;
    }

    private Pair<Set<Integer>, Set<Integer>> findCraftableSlots(T menu, Display display) {

        var clientRepo = menu.getClientRepo();
        if (clientRepo == null)
            return Pair.of(new HashSet<>(), new HashSet<>());

        var allEntries = clientRepo.getAllEntries();

        return Pair.of(
                findCraftableSlots(display.getInputEntries(), allEntries),
                findCraftableSlots(display.getOutputEntries(), allEntries));
    }

    private Set<Integer> findCraftableSlots(List<EntryIngredient> entries, Set<GridInventoryEntry> allEntries) {
        Set<Integer> craftableSlots = new HashSet<>();
        for (int i = 0, inputEntriesSize = entries.size(); i < inputEntriesSize; i++) {
            EntryIngredient entryStacks = entries.get(i);
            var itemIngredients = entryStacks.stream()
                    .filter(entryStack -> entryStack.getType() == VanillaEntryTypes.ITEM)
                    .map(entryStack -> (ItemStack) entryStack.castValue());
            var fluidIngredients = entryStacks.stream()
                    .filter(entryStack -> entryStack.getType() == VanillaEntryTypes.FLUID)
                    .map(entryStack -> (FluidStack) entryStack.castValue());
            boolean isCraftable = itemIngredients.parallel().anyMatch(ingredient -> allEntries.parallelStream()
                    .anyMatch(entry -> entry.isCraftable() && AEItemKey.matches(entry.getWhat(), ingredient))) ||
                    fluidIngredients.parallel().anyMatch(ingredient -> allEntries.parallelStream()
                            .anyMatch(entry -> entry.isCraftable() && AEFluidKey.matches(entry.getWhat(), ingredient)));
            if (isCraftable) {
                craftableSlots.add(i);
            }
        }
        return craftableSlots;
    }

    private static class IngredientVisibility {

        private final EntryRegistry registry;
        private final Map<ItemStack, Boolean> cache = new HashMap<>();

        private IngredientVisibility() {
            this.registry = EntryRegistry.getInstance();
        }

        private boolean isVisible(ItemStack stack) {
            if (cache.containsKey(stack)) {
                return cache.get(stack);
            }

            var entryStack = EntryStacks.of(stack);
            if (!registry.alreadyContain(entryStack)) {
                cache.put(stack, false);
                return false;
            }

            var entryStacks = registry.refilterNew(false, Collections.singleton(entryStack));
            var visible = !entryStacks.isEmpty();
            cache.put(stack, visible);
            return visible;
        }
    }

    private static TransferHandlerRenderer createErrorRenderer(Pair<Set<Integer>, Set<Integer>> craftableSlots) {
        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int inputIndex = 0;
            int outputIndex = 0;
            for (Widget widget : widgets) {
                if (widget instanceof Slot slot) {
                    boolean renderHighlight = false;
                    if (slot.getNoticeMark() == Slot.INPUT) {
                        if (craftableSlots.getLeft().contains(inputIndex)) {
                            renderHighlight = true;
                        }
                        inputIndex++;
                    }
                    if (slot.getNoticeMark() == Slot.OUTPUT) {
                        if (craftableSlots.getRight().contains(outputIndex)) {
                            renderHighlight = true;
                        }
                        outputIndex++;
                    }
                    if (renderHighlight) {
                        matrices.pushPose();
                        matrices.translate(0, 0, 400);
                        Rectangle innerBounds = slot.getInnerBounds();
                        GuiComponent.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(),
                                innerBounds.getMaxY(), EncodingHelper.BLUE_SLOT_HIGHLIGHT_COLOR);
                        matrices.popPose();
                    }
                }
            }
        };
    }
}
