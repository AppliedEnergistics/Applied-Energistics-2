package appeng.integration.modules.jei.transfer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.IShapedRecipe;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import appeng.core.localization.ItemModText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FillCraftingGridFromRecipePacket;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;

/**
 * Recipe transfer implementation with the intended purpose of actually crafting an item. Most of the work is done
 * server-side because permission-checks and inventory extraction cannot be done client-side.
 */
public class UseCraftingRecipeTransfer<T extends CraftingTermMenu>
        extends AbstractTransferHandler
        implements IRecipeTransferHandler<T, CraftingRecipe> {

    // Colors for the slot highlights
    private static final int BLUE_SLOT_HIGHLIGHT_COLOR = 0x400000ff;
    private static final int RED_SLOT_HIGHLIGHT_COLOR = 0x66ff0000;
    // Colors for the buttons
    private static final int BLUE_PLUS_BUTTON_COLOR = 0x804545FF;
    private static final int ORANGE_PLUS_BUTTON_COLOR = 0x80FFA500;

    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::getStoredAmount);
    private final Class<T> containerClass;
    private final IRecipeTransferHandlerHelper helper;

    public UseCraftingRecipeTransfer(Class<T> containerClass, IRecipeTransferHandlerHelper helper) {
        this.containerClass = containerClass;
        this.helper = helper;
    }

    @Override
    public IRecipeTransferError transferRecipe(T menu, CraftingRecipe recipe, IRecipeLayout display, Player player,
            boolean maxTransfer, boolean doTransfer) {
        if (recipe.getType() != RecipeType.CRAFTING) {
            return helper.createInternalError();
        }

        if (recipe.getIngredients().isEmpty()) {
            return helper.createUserErrorWithTooltip(ItemModText.INCOMPATIBLE_RECIPE.text());
        }

        if (!recipe.canCraftInDimensions(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT)) {
            return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        // Thank you RS for pioneering this amazing feature! :)
        boolean craftMissing = AbstractContainerScreen.hasControlDown();
        // Find missing ingredient
        var slotToIngredientMap = getGuiSlotToIngredientMap(recipe);
        var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));

        if (missingSlots.missingSlots().size() == slotToIngredientMap.size()) {
            // All missing, can't do much...
            return helper.createUserErrorForSlots(ItemModText.NO_ITEMS.text(), missingSlots.missingSlots());
        }

        // Find missing ingredients and highlight the slots which have these
        if (!doTransfer) {
            if (missingSlots.totalSize() != 0) {
                // Highlight the slots with missing ingredients.
                return new ErrorRenderer(menu, recipe);
            }
        } else {
            performTransfer(menu, recipe, craftMissing);
        }
        // No error
        return null;
    }

    protected void performTransfer(T menu, Recipe<?> recipe, boolean craftMissing) {

        // We send the items in the recipe in any case to serve as a fallback in case the recipe is transient
        var templateItems = findGoodTemplateItems(recipe, menu);

        var recipeId = recipe.getId();
        // Don't transmit a recipe id to the server in case the recipe is not actually resolvable
        // this is the case for recipes synthetically generated for JEI
        if (menu.getPlayer().level.getRecipeManager().byKey(recipe.getId()).isEmpty()) {
            AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
            recipeId = null;
        }

        NetworkHandler.instance()
                .sendToServer(new FillCraftingGridFromRecipePacket(recipeId, templateItems, craftMissing));
    }

    private NonNullList<ItemStack> findGoodTemplateItems(Recipe<?> recipe, MEStorageMenu menu) {
        var ingredientPriorities = getIngredientPriorities(menu, ENTRY_COMPARATOR);

        var templateItems = NonNullList.withSize(9, ItemStack.EMPTY);
        var ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
        for (int i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                // Try to find the best item. In case the ingredient is a tag, it might contain versions the
                // player doesn't actually have
                var stack = ingredientPriorities.entrySet()
                        .stream()
                        .filter(e -> e.getKey() instanceof AEItemKey itemKey && ingredient.test(itemKey.toStack()))
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .map(e -> ((AEItemKey) e.getKey()).toStack())
                        .orElse(ingredient.getItems()[0]);

                templateItems.set(i, stack);
            }
        }
        return templateItems;
    }

    private static Map<Integer, Ingredient> getGuiSlotToIngredientMap(Recipe<?> recipe) {
        var ingredients = recipe.getIngredients();

        // JEI will align non-shaped recipes smaller than 3x3 in the grid. It'll center them horizontally, and
        // some will be aligned to the bottom. (i.e. slab recipes).
        int width, height;
        if (recipe instanceof IShapedRecipe<?>shapedRecipe) {
            width = shapedRecipe.getRecipeWidth();
            height = shapedRecipe.getRecipeHeight();
        } else {
            if (ingredients.size() > 4) {
                width = height = 3;
            } else if (ingredients.size() > 1) {
                width = height = 2;
            } else {
                width = height = 1;
            }
        }

        var result = new HashMap<Integer, Ingredient>(ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            // JEI uses slot 0 for the output by default, shifting all input slots by 1
            var guiSlot = 1 + getCraftingIndex(i, width, height);
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                result.put(guiSlot, ingredient);
            }
        }
        return result;
    }

    private static int getCraftingIndex(int i, int width, int height) {
        int index;
        if (width == 1) {
            if (height == 3) {
                index = (i * 3) + 1;
            } else if (height == 2) {
                index = (i * 3) + 1;
            } else {
                index = 4;
            }
        } else if (height == 1) {
            index = i + 3;
        } else if (width == 2) {
            index = i;
            if (i > 1) {
                index++;
                if (i > 3) {
                    index++;
                }
            }
        } else if (height == 2) {
            index = i + 3;
        } else {
            index = i;
        }
        return index;
    }

    @Override
    public Class<T> getContainerClass() {
        return containerClass;
    }

    @Override
    public Class<CraftingRecipe> getRecipeClass() {
        return CraftingRecipe.class;
    }

    private record ErrorRenderer(CraftingTermMenu menu, Recipe<?> recipe) implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
                int recipeY) {
            // This needs to be recomputed every time since JEI reuses the error renderer.
            boolean craftMissing = AbstractContainerScreen.hasControlDown();
            var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));

            List<Component> extraTooltip = new ArrayList<>();
            if (missingSlots.anyCraftable()) {
                if (craftMissing) {
                    extraTooltip.add(ItemModText.WILL_CRAFT.text().withStyle(ChatFormatting.BLUE));
                } else {
                    extraTooltip.add(ItemModText.CTRL_CLICK_TO_CRAFT.text().withStyle(ChatFormatting.BLUE));
                }
            }
            if (missingSlots.anyMissing()) {
                extraTooltip.add(ItemModText.MISSING_ITEMS.text().withStyle(ChatFormatting.RED));
            }

            // 1) draw slot highlights
            IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
            var ingredients = itemStackGroup.getGuiIngredients();
            for (var entry : ingredients.entrySet()) {
                int i = entry.getKey();
                boolean missing = missingSlots.missingSlots().contains(i);
                boolean craftable = missingSlots.craftableSlots().contains(i);
                if (missing || craftable) {
                    entry.getValue().drawHighlight(poseStack,
                            missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR, recipeX, recipeY);
                }
            }
            // 2) draw tooltip
            drawHoveringText(poseStack, extraTooltip, mouseX, mouseY);
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
