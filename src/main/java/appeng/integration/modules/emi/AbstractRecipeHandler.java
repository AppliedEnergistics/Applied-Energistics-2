package appeng.integration.modules.emi;

import static appeng.integration.modules.itemlists.TransferHelper.BLUE_SLOT_HIGHLIGHT_COLOR;
import static appeng.integration.modules.itemlists.TransferHelper.RED_SLOT_HIGHLIGHT_COLOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;

import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.integration.modules.itemlists.TransferHelper;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;

abstract class AbstractRecipeHandler<T extends AEBaseMenu> implements StandardRecipeHandler<T> {
    protected static final int CRAFTING_GRID_WIDTH = 3;
    protected static final int CRAFTING_GRID_HEIGHT = 3;

    /**
     * Reused by {@link #canCraftWith}, which runs for every candidate recipe.
     */
    private static final ThreadLocal<Scratch> SCRATCH = ThreadLocal.withInitial(Scratch::new);

    private final Class<T> containerClass;

    /**
     * Everything EMI derives from one screen, kept only while that screen is open. The screens are weakly referenced so
     * that closing a terminal releases the converted stacks of a large network instead of retaining them until the next
     * terminal is opened.
     */
    private final Map<AbstractContainerScreen<T>, ScreenCache> screenCaches = new WeakHashMap<>();

    AbstractRecipeHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public List<Slot> getInputSources(T menu) {
        var slots = new ArrayList<Slot>();
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_INVENTORY));
        slots.addAll(menu.getSlots(SlotSemantics.PLAYER_HOTBAR));
        slots.addAll(menu.getSlots(SlotSemantics.CRAFTING_GRID));
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(T menu) {
        return menu.getSlots(SlotSemantics.CRAFTING_GRID);
    }

    @Override
    public @Nullable Slot getOutputSlot(T menu) {
        for (var slot : menu.getSlots(SlotSemantics.CRAFTING_RESULT)) {
            return slot;
        }
        return null;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
        var menu = screen.getMenu();
        var slots = getInputSources(menu);
        var carried = menu.getCarried();

        // Everything EMI derives from the inventory has to be covered here, or cached values go stale
        var fingerprint = 17;
        for (var slot : slots) {
            var stack = slot.getItem();
            fingerprint = 31 * fingerprint + ItemStack.hashItemAndComponents(stack);
            fingerprint = 31 * fingerprint + stack.getCount();
        }
        fingerprint = 31 * fingerprint + ItemStack.hashItemAndComponents(carried);
        fingerprint = 31 * fingerprint + carried.getCount();

        var repo = menu instanceof MEStorageMenu storageMenu ? storageMenu.getClientRepo() : null;
        var revision = repo != null ? repo.getRevision() : -1;

        // EMI asks for the inventory far more often than it changes, so it is only rebuilt once the network revision or
        // the relevant slots change
        var cache = screenCaches.get(screen);
        if (cache == null) {
            cache = new ScreenCache();
            screenCaches.put(screen, cache);
        }

        if (cache.inventory != null
                && cache.revision == revision
                && cache.fingerprint == fingerprint) {
            return cache.inventory;
        }

        var list = new ArrayList<EmiStack>(slots.size() + (repo != null ? repo.getAllEntries().size() : 0));

        // Passed separately so the craftables cache can tell slot changes from network changes
        var slotStacks = new ArrayList<EmiStack>(slots.size());
        for (var slot : slots) {
            var stack = EmiStack.of(slot.getItem());
            list.add(stack);
            if (!stack.isEmpty()) {
                slotStacks.add(stack);
            }
        }
        var carriedStack = EmiStack.of(carried);
        if (!carriedStack.isEmpty()) {
            slotStacks.add(carriedStack);
        }

        if (repo != null) {
            var converters = EmiStackConverters.getConverters();
            var networkStacks = cache.networkStacks;
            var seenKeys = cache.seenKeys;
            var present = 0;
            seenKeys.clear();

            for (var entry : repo.getAllEntries()) {
                var amount = entry.getStoredAmount();
                if (amount <= 0) {
                    continue; // Skip items that are only craftable
                }

                var what = entry.getWhat();
                seenKeys.add(what);

                var emiStack = networkStacks.get(what);
                if (emiStack == null) {
                    emiStack = EmiStackHelper.toEmiStack(new GenericStack(what, amount), converters);
                    if (emiStack == null) {
                        continue;
                    }
                    networkStacks.put(what, emiStack);
                } else if (emiStack.getAmount() != amount) {
                    // EMI's constructor adds up the amounts of stacks that are equal to each other, so a stack that was
                    // merged into another one has to be reset to the amount that is stored in the network
                    emiStack.setAmount(amount);
                }
                present++;
                list.add(emiStack);
            }

            if (networkStacks.size() > present) {
                // Entries for keys that are no longer in the network
                networkStacks.keySet().retainAll(seenKeys);
            }
        }

        var inventory = new NetworkEmiPlayerInventory(list, slotStacks, cache.craftables, revision, fingerprint, repo);
        cache.inventory = inventory;
        cache.revision = revision;
        cache.fingerprint = fingerprint;
        return inventory;
    }

    /**
     * The state EMI derives from a screen, and nothing else. The inventory is not retained anywhere but here, so it is
     * released together with the screen.
     */
    private static final class ScreenCache {
        private @Nullable EmiPlayerInventory inventory;
        private long revision;
        private int fingerprint;

        /**
         * Outlives the inventory instances EMI asks for.
         */
        private final CraftablesCache craftables = new CraftablesCache();

        /**
         * One converted stack per network key, reused across rebuilds to avoid converting the whole network again. The
         * amounts are shared with the inventory that EMI was last given, see the reset in {@link #getInventory}.
         */
        private final Map<AEKey, EmiStack> networkStacks = new HashMap<>();

        /**
         * The network keys seen during the last rebuild, to drop the converted stacks of keys that are gone.
         */
        private final Set<AEKey> seenKeys = new HashSet<>();
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
        if (context.getType() == EmiCraftContext.Type.FILL_BUTTON) {
            return transferRecipe(recipe, context, false).canCraft();
        }
        return canCraftWith(context.getInventory(), recipe);
    }

    /**
     * Same result as {@link EmiPlayerInventory#canCraft(EmiRecipe)}, without EMI's per-recipe map and list allocations.
     */
    private static boolean canCraftWith(EmiPlayerInventory inventory, EmiRecipe recipe) {
        var scratch = SCRATCH.get();
        scratch.reset();

        for (var ingredient : recipe.getInputs()) {
            if (ingredient.isEmpty()) {
                continue;
            }

            var satisfied = false;
            if (ingredient instanceof EmiStack single) {
                // getEmiStacks() allocates a list, so fast-path the common case
                satisfied = scratch.consume(inventory, single);
            } else {
                for (var stack : ingredient.getEmiStacks()) {
                    if (scratch.consume(inventory, stack)) {
                        satisfied = true;
                        break;
                    }
                }
            }

            if (!satisfied) {
                return false;
            }
        }
        return true;
    }

    /**
     * What the ingredients of one recipe consumed so far, so a recipe needing four of an item isn't craftable from one.
     */
    private static final class Scratch {
        private EmiStack[] stacks = new EmiStack[16];
        private long[] amounts = new long[16];
        private int count;

        void reset() {
            count = 0;
        }

        boolean consume(EmiPlayerInventory inventory, EmiStack stack) {
            // Key and value are the same instance, so identity comparison is enough
            var identity = inventory.inventory.get(stack);
            if (identity == null) {
                return false;
            }

            var index = -1;
            var alreadyUsed = 0L;
            for (var i = 0; i < count; i++) {
                if (stacks[i] == identity) {
                    index = i;
                    alreadyUsed = amounts[i];
                    break;
                }
            }

            if (identity.getAmount() - alreadyUsed < stack.getAmount()) {
                return false;
            }

            if (index >= 0) {
                amounts[index] = alreadyUsed + stack.getAmount();
            } else {
                if (count == stacks.length) {
                    stacks = Arrays.copyOf(stacks, count * 2);
                    amounts = Arrays.copyOf(amounts, count * 2);
                }
                stacks[count] = identity;
                amounts[count] = stack.getAmount();
                count++;
            }
            return true;
        }
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

        var result = transferRecipe(menu, holder, emiRecipe, doTransfer);
        if (result instanceof Result.Success && doTransfer) {
            Minecraft.getInstance().setScreen(context.getScreen());
        }
        return result;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        return transferRecipe(recipe, context, true).canCraft();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<T> context) {
        var tooltip = transferRecipe(recipe, context, false).getTooltip(recipe, context);
        if (tooltip != null) {
            return tooltip.stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .toList();
        } else {
            return StandardRecipeHandler.super.getTooltip(recipe, context);
        }
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, GuiGraphics draw) {
        transferRecipe(recipe, context, false).render(recipe, context, widgets, draw);
    }

    @Nullable
    private RecipeHolder<?> getRecipeHolder(Level level, EmiRecipe recipe) {
        if (recipe.getBackingRecipe() != null) {
            return recipe.getBackingRecipe();
        }
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
        /**
         * @return null doesn't override the default tooltip.
         */
        @Nullable
        List<Component> getTooltip(EmiRecipe recipe, EmiCraftContext<?> context) {
            return null;
        }

        abstract boolean canCraft();

        void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets,
                GuiGraphics draw) {
        }

        static final class Success extends Result {
            @Override
            boolean canCraft() {
                return true;
            }
        }

        /**
         * There are missing ingredients, but at least one is present.
         */
        static final class PartiallyCraftable extends Result {
            private final CraftingTermMenu.MissingIngredientSlots missingSlots;

            public PartiallyCraftable(CraftingTermMenu.MissingIngredientSlots missingSlots) {
                this.missingSlots = missingSlots;
            }

            @Override
            boolean canCraft() {
                return true;
            }

            @Override
            List<Component> getTooltip(EmiRecipe recipe, EmiCraftContext<?> context) {
                // EMI caches this tooltip, we cannot dynamically react to control being held here
                return TransferHelper.createCraftingTooltip(missingSlots, false, false);
            }

            @Override
            void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets,
                    GuiGraphics guiGraphics) {
                renderMissingAndCraftableSlotOverlays(getRecipeInputSlots(recipe, widgets), guiGraphics,
                        missingSlots.missingSlots(),
                        missingSlots.craftableSlots());
            }
        }

        /**
         * Indicates that some of the slots can already be crafted by the auto-crafting system.
         */
        static final class EncodeWithCraftables extends Result {
            private final Set<AEKey> craftableKeys;

            /**
             * @param craftableKeys All keys that the current system can auto-craft.
             */
            public EncodeWithCraftables(Set<AEKey> craftableKeys) {
                this.craftableKeys = craftableKeys;
            }

            @Override
            boolean canCraft() {
                return true;
            }

            @Override
            List<Component> getTooltip(EmiRecipe emiRecipe, EmiCraftContext<?> context) {
                var anyCraftable = emiRecipe.getInputs().stream()
                        .anyMatch(ing -> isCraftable(craftableKeys, ing));
                if (anyCraftable) {
                    return TransferHelper.createEncodingTooltip(true, false);
                }
                return null;
            }

            @Override
            void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets,
                    GuiGraphics guiGraphics) {
                for (var widget : widgets) {
                    if (widget instanceof SlotWidget slot && isInputSlot(slot)) {
                        if (isCraftable(craftableKeys, slot.getStack())) {
                            var poseStack = guiGraphics.pose();
                            poseStack.pushPose();
                            poseStack.translate(0, 0, 400);
                            var bounds = getInnerBounds(slot);
                            guiGraphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(),
                                    BLUE_SLOT_HIGHLIGHT_COLOR);
                            poseStack.popPose();
                        }
                    }
                }
            }

            private static boolean isCraftable(Set<AEKey> craftableKeys, EmiIngredient ingredient) {
                return ingredient.getEmiStacks().stream().anyMatch(emiIngredient -> {
                    var stack = EmiStackHelper.toGenericStack(emiIngredient);
                    return stack != null && craftableKeys.contains(stack.what());
                });
            }
        }

        static final class NotApplicable extends Result {
            @Override
            boolean canCraft() {
                return false;
            }
        }

        static final class Error extends Result {
            private final Component message;
            private final Set<Integer> missingSlots;

            public Error(Component message, Set<Integer> missingSlots) {
                this.message = message;
                this.missingSlots = missingSlots;
            }

            public Component getMessage() {
                return message;
            }

            @Override
            boolean canCraft() {
                return false;
            }

            @Override
            void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets,
                    GuiGraphics guiGraphics) {

                renderMissingAndCraftableSlotOverlays(getRecipeInputSlots(recipe, widgets), guiGraphics, missingSlots,
                        Set.of());
            }
        }

        static NotApplicable createNotApplicable() {
            return new NotApplicable();
        }

        static Success createSuccessful() {
            return new Success();
        }

        static Error createFailed(Component text) {
            return new Error(text, Set.of());
        }

        static Error createFailed(Component text, Set<Integer> missingSlots) {
            return new Error(text, missingSlots);
        }
    }

    private static void renderMissingAndCraftableSlotOverlays(Map<Integer, SlotWidget> inputSlots,
            GuiGraphics guiGraphics,
            Set<Integer> missingSlots, Set<Integer> craftableSlots) {
        for (var entry : inputSlots.entrySet()) {
            boolean missing = missingSlots.contains(entry.getKey());
            boolean craftable = craftableSlots.contains(entry.getKey());
            if (missing || craftable) {
                var poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(0, 0, 400);
                var innerBounds = getInnerBounds(entry.getValue());
                guiGraphics.fill(innerBounds.x(), innerBounds.y(), innerBounds.right(),
                        innerBounds.bottom(), missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR);
                poseStack.popPose();
            }
        }
    }

    private static boolean isInputSlot(SlotWidget slot) {
        return slot.getRecipe() == null;
    }

    private static Bounds getInnerBounds(SlotWidget slot) {
        var bounds = slot.getBounds();
        return new Bounds(
                bounds.x() + 1,
                bounds.y() + 1,
                bounds.width() - 2,
                bounds.height() - 2);
    }

    private static Map<Integer, SlotWidget> getRecipeInputSlots(EmiRecipe recipe, List<Widget> widgets) {
        // Map ingredient indices to their respective slots
        var inputSlots = new HashMap<Integer, SlotWidget>(recipe.getInputs().size());
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            for (var widget : widgets) {
                if (widget instanceof SlotWidget slot && isInputSlot(slot)) {
                    if (slot.getStack() == recipe.getInputs().get(i)) {
                        inputSlots.put(i, slot);
                    }
                }
            }
        }
        return inputSlots;
    }
}
