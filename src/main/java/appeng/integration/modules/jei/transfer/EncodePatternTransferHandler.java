package appeng.integration.modules.jei.transfer;

import java.util.*;

import appeng.api.stacks.AEFluidKey;
import com.google.common.math.LongMath;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientVisibility;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.util.CraftingRecipeUtil;

/**
 * Handles encoding patterns in the {@link PatternEncodingTermMenu} by clicking the + button on recipes shown in REI (or
 * JEI).
 */
public class EncodePatternTransferHandler<T extends PatternEncodingTermMenu>
        extends AbstractTransferHandler
        implements IRecipeTransferHandler<T, Object> {
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;
    private static final int BLUE_SLOT_HIGHLIGHT_COLOR = 0x400000ff;

    /**
     * Order of priority: - Craftable Items - Undamaged Items - Items the player has the most of
     */
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::isCraftable)
            .thenComparing(EncodePatternTransferHandler::isUndamaged)
            .thenComparing(GridInventoryEntry::getStoredAmount);

    private static Boolean isUndamaged(GridInventoryEntry entry) {
        return !(entry.getWhat() instanceof AEItemKey itemKey) || !itemKey.isDamaged();
    }

    private final Class<T> containerClass;
    private final IRecipeTransferHandlerHelper helper;
    @Nullable
    private IIngredientVisibility ingredientVisibility;

    public EncodePatternTransferHandler(Class<T> containerClass, IRecipeTransferHandlerHelper helper) {
        this.containerClass = containerClass;
        this.helper = helper;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(T menu, Object recipeBase, IRecipeLayout recipeLayout, Player player,
                                               boolean maxTransfer, boolean doTransfer) {

        // Recipe displays can be based on anything. Not just Recipe<?>
        Recipe<?> recipe = null;
        if (recipeBase instanceof Recipe<?>) {
            recipe = (Recipe<?>) recipeBase;
        }

        // Crafting recipe slots are not grouped, hence they must fit into the 3x3 grid.
        boolean craftingRecipe = isCraftingRecipe(recipe, recipeLayout);
        if (craftingRecipe && !fitsIn3x3Grid(recipe, recipeLayout)) {
            return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (craftingRecipe) {
                menu.setMode(EncodingMode.CRAFTING);
                encodeCraftingRecipe(menu, recipe, getGuiIngredientsForCrafting(recipeLayout));
            } else {
                menu.setMode(EncodingMode.PROCESSING);
                encodeProcessingRecipe(menu,
                        GenericEntryStackHelper.ofInputs(recipeLayout),
                        GenericEntryStackHelper.ofOutputs(recipeLayout));
            }
        }else {
            Set<Integer> craftableSlots = findCraftableSlots(menu, recipeLayout);
            if (!craftableSlots.isEmpty()) {
                return new CraftableIngredientError(craftableSlots);
            }
        }

        return null;
    }

    /**
     * In case the recipe does not report inputs, we will use the inputs shown on the JEI GUI instead.
     */
    private List<List<GenericStack>> getGuiIngredientsForCrafting(IRecipeLayout recipeLayout) {
        var result = new ArrayList<List<GenericStack>>(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT);
        for (int i = 0; i < CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT; i++) {
            var stacks = new ArrayList<GenericStack>();

            var guiIngredient = recipeLayout.getItemStacks().getGuiIngredients().get(i);
            if (guiIngredient != null) {
                for (var stack : guiIngredient.getAllIngredients()) {
                    stacks.add(GenericStack.fromItemStack(stack));
                }
            }

            result.add(stacks);
        }

        return result;
    }

    private void encodeProcessingRecipe(T menu, List<List<GenericStack>> genericIngredients,
                                        List<GenericStack> genericResults) {
        // Note that this runs on the client and getClientRepo() is guaranteed to be available there.
        var ingredientPriorities = getIngredientPriorities(menu, ENTRY_COMPARATOR);

        encodeBestMatchingStacksIntoSlots(
                genericIngredients,
                ingredientPriorities,
                menu.getProcessingInputSlots());
        encodeBestMatchingStacksIntoSlots(
                // For the outputs, it's only one possible item per slot
                genericResults.stream().map(List::of).toList(),
                ingredientPriorities,
                menu.getProcessingOutputSlots());
    }

    private void encodeBestMatchingStacksIntoSlots(List<List<GenericStack>> possibleInputsBySlot,
                                                   Map<AEKey, Integer> ingredientPriorities,
                                                   FakeSlot[] slots) {
        var encodedInputs = new ArrayList<GenericStack>();
        for (var genericIngredient : possibleInputsBySlot) {
            if (!genericIngredient.isEmpty()) {
                addOrMerge(encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient));
            }
        }

        for (int i = 0; i < slots.length; i++) {
            var slot = slots[i];
            var stack = (i < encodedInputs.size()) ? GenericStack.wrapInItemStack(encodedInputs.get(i))
                    : ItemStack.EMPTY;
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, slot.index, stack));
        }
    }

    private void encodeCraftingRecipe(T menu,
                                      @Nullable Recipe<?> recipe,
                                      List<List<GenericStack>> genericIngredients) {
        // Note that this runs on the client and getClientRepo() is guaranteed to be available there.
        var prioritizedNetworkInv = getIngredientPriorities(menu, ENTRY_COMPARATOR);

        var encodedInputs = NonNullList.withSize(menu.getCraftingGridSlots().length, ItemStack.EMPTY);

        if (recipe != null) {
            // Cache the ingredient visibility instance for checks for the best ingredient.
            if (ingredientVisibility == null) {
                ingredientVisibility = JEIFacade.instance().getRuntime().getIngredientVisibility();
            }

            // When we have access to a crafting recipe, we'll switch modes and try to find suitable
            // ingredients based on the recipe ingredients, which allows for fuzzy-matching.
            var ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);

            // Find a good match for every ingredient
            for (int slot = 0; slot < ingredients3x3.size(); slot++) {
                var ingredient = ingredients3x3.get(slot);
                if (ingredient.isEmpty()) {
                    continue; // Skip empty slots
                }

                // Due to how some crafting recipes work, the ingredient can match more than just one item in the
                // network inventory. We'll find all network inventory entries that it matches and sort them
                // according to their suitability for encoding a pattern
                var bestNetworkIngredient = prioritizedNetworkInv.entrySet().stream()
                        .filter(ni -> ni.getKey() instanceof AEItemKey itemKey && ingredient.test(itemKey.toStack()))
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .map(entry -> entry.getKey() instanceof AEItemKey itemKey ? itemKey.toStack() : null);

                // To avoid encoding hidden entries, we'll cycle through the ingredient and try to find a visible
                // stack, otherwise we'll use the first entry.
                var bestIngredient = bestNetworkIngredient.orElseGet(() -> {
                    for (var stack : ingredient.getItems()) {
                        if (ingredientVisibility.isIngredientVisible(VanillaTypes.ITEM_STACK, stack)) {
                            return stack;
                        }
                    }
                    return ingredient.getItems()[0];
                });

                encodedInputs.set(slot, bestIngredient);
            }
        } else {
            for (int slot = 0; slot < genericIngredients.size(); slot++) {
                var genericIngredient = genericIngredients.get(slot);
                if (genericIngredient.isEmpty()) {
                    continue; // Skip empty slots
                }

                var bestIngredient = findBestIngredient(prioritizedNetworkInv, genericIngredient).what();

                // Clamp amounts to 1 in crafting table mode
                if (bestIngredient instanceof AEItemKey itemKey) {
                    encodedInputs.set(slot, itemKey.toStack());
                } else {
                    encodedInputs.set(slot, GenericStack.wrapInItemStack(bestIngredient, 1));
                }
            }
        }

        for (int i = 0; i < encodedInputs.size(); i++) {
            ItemStack encodedInput = encodedInputs.get(i);
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, menu.getCraftingGridSlots()[i].index, encodedInput));
        }

        // Clear out the processing outputs
        for (var outputSlot : menu.getProcessingOutputSlots()) {
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, outputSlot.index, ItemStack.EMPTY));
        }

    }

    // Given a set of possible ingredients, find the one that has the highest priority
    private static GenericStack findBestIngredient(Map<AEKey, Integer> ingredientPriorities,
                                                   List<GenericStack> possibleIngredients) {
        return possibleIngredients.stream()
                .map(gi -> Pair.of(gi, ingredientPriorities.getOrDefault(gi.what(), Integer.MIN_VALUE)))
                .max(Comparator.comparingInt(Pair::getRight))
                .map(Pair::getLeft)
                .orElseThrow();
    }

    /**
     * In processing mode it makes sense to merge stacks of the same type together.
     */
    private void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
        for (int i = 0; i < stacks.size(); i++) {
            var existingStack = stacks.get(i);
            if (Objects.equals(existingStack.what(), newStack.what())) {
                // Add the new amount onto the existing amount
                long newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
                stacks.set(i, new GenericStack(newStack.what(), newAmount));

                // Determine if the addition overflowed. If it did, add the remainder as a new stack.
                long overflow = newStack.amount() - (newAmount - existingStack.amount());
                if (overflow > 0) {
                    stacks.add(new GenericStack(newStack.what(), overflow));
                }
                return;
            }
        }

        stacks.add(newStack);
    }

    private Set<Integer> findCraftableSlots(T menu, IRecipeLayout recipeLayout) {
        var clientRepo = menu.getClientRepo();
        if (clientRepo == null) return Collections.emptySet();

        Set<Integer> craftableSlots = new HashSet<>();
        var itemIngredients = recipeLayout.getItemStacks().getGuiIngredients();
        var fluidIngredient = recipeLayout.getFluidStacks().getGuiIngredients();
        var allEntries = menu.getClientRepo().getAllEntries();
        itemIngredients.forEach((key, value) -> {
            var ingredients = value.getAllIngredients();
            boolean isCraftable = ingredients.parallelStream()
                    .anyMatch(ingredient -> allEntries.parallelStream()
                            .anyMatch(menuEntry -> AEItemKey.matches(menuEntry.getWhat(), ingredient) && menuEntry.isCraftable())
                    );
            if (isCraftable) {
                craftableSlots.add(key);
            }
        });
        fluidIngredient.forEach((key, value) -> {
            var ingredients = value.getAllIngredients();
            boolean isCraftable = ingredients.parallelStream()
                    .anyMatch(ingredient -> allEntries.parallelStream()
                            .anyMatch(menuEntry -> AEFluidKey.matches(menuEntry.getWhat(), ingredient) && menuEntry.isCraftable())
                    );
            if (isCraftable) {
                craftableSlots.add(key);
            }
        });
        return craftableSlots;
    }

    @Override
    public Class<T> getContainerClass() {
        return containerClass;
    }

    @Override
    public Class<Object> getRecipeClass() {
        return Object.class;
    }

    private record CraftableIngredientError(Set<Integer> craftableSlots) implements IRecipeTransferError {

        @Override
        public @NotNull Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public void showError(@NotNull PoseStack poseStack, int mouseX, int mouseY, @NotNull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
            var itemIngredients = recipeLayout.getItemStacks().getGuiIngredients();
            var fluidIngredient = recipeLayout.getFluidStacks().getGuiIngredients();
            List<IGuiIngredient<?>> craftableIngredients = new ArrayList<>();
            for (Integer slot : craftableSlots) {
                if (itemIngredients.containsKey(slot)) {
                    craftableIngredients.add(itemIngredients.get(slot));
                } else if (fluidIngredient.containsKey(slot)) {
                    craftableIngredients.add(fluidIngredient.get(slot));
                }
            }
            for (var ingredient : craftableIngredients) {
                ingredient.drawHighlight(poseStack,BLUE_SLOT_HIGHLIGHT_COLOR ,recipeX, recipeY);
            }
            drawHoveringText(poseStack, Collections.singletonList(ItemModText.INGREDIENT_CRAFTABLE.text().withStyle(ChatFormatting.BLUE)), mouseX, mouseY);
        }

        // Copy-pasted from JEI since it doesn't seem to expose these
        public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            drawHoveringText(poseStack, textLines, x, y, ItemStack.EMPTY, font);
        }

        private static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y,
                                             ItemStack itemStack, Font font) {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = minecraft.screen;
            if (screen == null) {
                return;
            }

            Optional<TooltipComponent> tooltipImage = itemStack.getTooltipImage();
            screen.renderTooltip(poseStack, textLines, tooltipImage, x, y, font, itemStack);
        }
    }

}
