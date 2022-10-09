package appeng.integration.modules.jei.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientVisibility;

import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jei.JEIPlugin;
import appeng.integration.modules.jei.JeiRuntimeAdapter;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;

/**
 * Handles encoding patterns in the {@link PatternEncodingTermMenu} by clicking the + button on recipes shown in REI (or
 * JEI).
 */
public class EncodePatternTransferHandler<T extends PatternEncodingTermMenu>
        extends AbstractTransferHandler
        implements IRecipeTransferHandler<T, Object> {
    private static final int CRAFTING_GRID_WIDTH = 3;
    private static final int CRAFTING_GRID_HEIGHT = 3;

    private final MenuType<T> menuType;
    private final Class<T> menuClass;
    private final IRecipeTransferHandlerHelper helper;
    @Nullable
    private IIngredientVisibility ingredientVisibility;

    public EncodePatternTransferHandler(MenuType<T> menuType,
            Class<T> menuClass,
            IRecipeTransferHandlerHelper helper) {
        this.menuType = menuType;
        this.menuClass = menuClass;
        this.helper = helper;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(T menu, Object recipeBase, IRecipeSlotsView slotsView, Player player,
            boolean maxTransfer, boolean doTransfer) {

        // Recipe displays can be based on anything. Not just Recipe<?>
        Recipe<?> recipe = null;
        if (recipeBase instanceof Recipe<?>) {
            recipe = (Recipe<?>) recipeBase;
        }

        // Crafting recipe slots are not grouped, hence they must fit into the 3x3 grid.
        boolean craftingRecipe = EncodingHelper.isSupportedCraftingRecipe(recipe);
        if (craftingRecipe && !fitsIn3x3Grid(recipe, slotsView)) {
            return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (craftingRecipe) {
                EncodingHelper.encodeCraftingRecipe(
                        menu,
                        recipe,
                        getGuiIngredientsForCrafting(slotsView),
                        this::isIngredientVisible);
            } else {
                EncodingHelper.encodeProcessingRecipe(menu,
                        GenericEntryStackHelper.ofInputs(slotsView),
                        GenericEntryStackHelper.ofOutputs(slotsView));
            }
        } else {
            var craftableSlots = findCraftableSlots(menu, slotsView);
            return new ErrorRenderer(craftableSlots);
        }

        return null;
    }

    private boolean isIngredientVisible(ItemStack itemStack) {
        // Cache the ingredient visibility instance for checks for the best ingredient.
        if (ingredientVisibility == null) {
            ingredientVisibility = ((JeiRuntimeAdapter) JEIFacade.instance()).getRuntime().getIngredientVisibility();
        }
        return ingredientVisibility.isIngredientVisible(VanillaTypes.ITEM_STACK, itemStack);
    }

    /**
     * In case the recipe does not report inputs, we will use the inputs shown on the JEI GUI instead.
     */
    private List<List<GenericStack>> getGuiIngredientsForCrafting(IRecipeSlotsView recipeLayout) {
        var recipeSlots = recipeLayout.getSlotViews();

        var result = new ArrayList<List<GenericStack>>(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT);
        for (int i = 0; i < CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT; i++) {
            if (i < recipeSlots.size()) {
                var slot = recipeSlots.get(i);
                result.add(slot.getIngredients(VanillaTypes.ITEM_STACK)
                        .map(GenericStack::fromItemStack)
                        .filter(Objects::nonNull)
                        .toList());
            } else {
                result.add(Collections.emptyList());
            }
        }

        return result;
    }

    private List<IRecipeSlotView> findCraftableSlots(T menu, IRecipeSlotsView slotsView) {
        var repo = menu.getClientRepo();
        if (repo == null) {
            return List.of();
        }

        var craftableKeys = repo.getAllEntries().stream()
                .filter(GridInventoryEntry::isCraftable)
                .map(GridInventoryEntry::getWhat)
                .collect(Collectors.toSet());

        return slotsView.getSlotViews(RecipeIngredientRole.INPUT).stream()
                .filter(slotView -> slotView.getAllIngredients().anyMatch(ingredient -> {
                    var stack = GenericEntryStackHelper.ingredientToStack(ingredient);
                    return stack != null && craftableKeys.contains(stack.what());
                }))
                .toList();
    }

    @Override
    public Optional<MenuType<T>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public Class<? extends T> getContainerClass() {
        return menuClass;
    }

    @Override
    public RecipeType<Object> getRecipeType() {
        // This is not actually used, as we register with JEI as a universal handler
        return null;
    }

    private record ErrorRenderer(List<IRecipeSlotView> craftableSlots) implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public int getButtonHighlightColor() {
            return 0; // We never want the orange highlight!
        }

        @Override
        public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView,
                int recipeX, int recipeY) {
            poseStack.pushPose();
            poseStack.translate(recipeX, recipeY, 0);

            for (IRecipeSlotView slotView : craftableSlots) {
                slotView.drawHighlight(poseStack, TransferHelper.BLUE_SLOT_HIGHLIGHT_COLOR);
            }

            poseStack.popPose();

            JEIPlugin.drawHoveringText(poseStack, TransferHelper.createEncodingTooltip(!craftableSlots.isEmpty()),
                    mouseX, mouseY);
        }
    }
}
