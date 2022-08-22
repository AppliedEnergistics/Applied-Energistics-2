package appeng.integration.modules.jei.transfer;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IIngredientVisibility;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
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
        boolean craftingRecipe = isCraftingRecipe(recipe, slotsView);
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
            Set<IRecipeSlotView> craftableSlots = findCraftableSlots(menu, slotsView);
            if (!craftableSlots.isEmpty()) {
                return new CraftableIngredientError(craftableSlots);
            }
        }

        return null;
    }

    private boolean isIngredientVisible(ItemStack itemStack) {
        // Cache the ingredient visibility instance for checks for the best ingredient.
        if (ingredientVisibility == null) {
            ingredientVisibility = JEIFacade.instance().getRuntime().getIngredientVisibility();
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

    private Set<IRecipeSlotView> findCraftableSlots(T menu, IRecipeSlotsView slotsView) {
        var clientRepo = menu.getClientRepo();
        if (clientRepo == null)
            return Collections.emptySet();
        Set<IRecipeSlotView> craftableSlots = new HashSet<>();
        // How do I check the other AEKeys?
        var allEntries = clientRepo.getAllEntries();
        for (IRecipeSlotView slotView : slotsView.getSlotViews(RecipeIngredientRole.INPUT)) {
            var itemIngredients = slotView.getItemStacks();
            var fluidIngredients = slotView.getIngredients(ForgeTypes.FLUID_STACK);
            boolean isCraftable = itemIngredients.parallel().anyMatch(ingredient -> allEntries.parallelStream()
                    .anyMatch(entry -> entry.isCraftable() && AEItemKey.matches(entry.getWhat(), ingredient))) ||
                    fluidIngredients.parallel().anyMatch(ingredient -> allEntries.parallelStream()
                            .anyMatch(entry -> entry.isCraftable() && AEFluidKey.matches(entry.getWhat(), ingredient)));
            if (isCraftable) {
                craftableSlots.add(slotView);
            }
        }

        return craftableSlots;
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

    private record CraftableIngredientError(Set<IRecipeSlotView> craftableSlots) implements IRecipeTransferError {

        @Override
        public @NotNull Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public void showError(@NotNull PoseStack poseStack, int mouseX, int mouseY,
                @NotNull IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
            poseStack.pushPose();
            poseStack.translate(recipeX, recipeY, 0);
            for (IRecipeSlotView slotView : craftableSlots) {
                slotView.drawHighlight(poseStack, EncodingHelper.BLUE_SLOT_HIGHLIGHT_COLOR);
            }
            poseStack.popPose();
            drawHoveringText(poseStack,
                    Collections.singletonList(ItemModText.INGREDIENT_CRAFTABLE.text().withStyle(ChatFormatting.BLUE)),
                    mouseX, mouseY);
        }

        // Copy-pasted from JEI since it doesn't seem to expose these
        public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
            var minecraft = Minecraft.getInstance();
            var screen = minecraft.screen;
            if (screen == null) {
                return;
            }

            screen.renderTooltip(poseStack, textLines, Optional.empty(), x, y);
        }
    }

}
