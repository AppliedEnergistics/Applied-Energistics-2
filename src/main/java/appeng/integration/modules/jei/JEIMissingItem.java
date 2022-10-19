package appeng.integration.modules.jei;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collection;

import static mezz.jei.api.recipe.transfer.IRecipeTransferError.Type.USER_FACING;

public class JEIMissingItem implements IRecipeTransferError {

    private boolean errored;

    JEIMissingItem(Container container, @Nonnull IRecipeLayout recipeLayout) {
        if (container instanceof ContainerCraftingTerm) {
            ContainerCraftingTerm craftingTerm = (ContainerCraftingTerm) container;
            GuiCraftingTerm g = (GuiCraftingTerm) craftingTerm.getGui();

            IItemList<IAEItemStack> ir = g.getRepo().getList();
            boolean found;
            this.errored = false;

            recipeLayout.getItemStacks().addTooltipCallback(new CraftableCallBack(container, ir));

            for (IGuiIngredient<?> i : recipeLayout.getItemStacks().getGuiIngredients().values()) {
                found = false;
                if (i.isInput() && i.getAllIngredients().size() > 0) {
                    for (Object o : i.getAllIngredients()) {
                        if (o instanceof ItemStack) {
                            ItemStack stack = (ItemStack) o;
                            if (!stack.isEmpty()) {
                                if (stack.getItem().isDamageable() || Platform.isGTDamageableItem(stack.getItem())) {
                                    Collection<IAEItemStack> fuzzy = ir.findFuzzy(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL);
                                    if (fuzzy.size() > 0) {
                                        for (IAEItemStack itemStack : fuzzy) {
                                            if (itemStack.getStackSize() > 0) {
                                                if (itemStack.fuzzyComparison(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL)) {
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    IAEItemStack ext = ir.findPrecise(AEItemStack.fromItemStack(stack));
                                    if (ext != null) {
                                        if (ext.getStackSize() > 0) {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!found) {
                        this.errored = true;
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public Type getType() {
        return USER_FACING;
    }

    @Override
    public void showError(Minecraft minecraft, int mouseX, int mouseY, @Nonnull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
        Container c = minecraft.player.openContainer;
        if (c instanceof ContainerCraftingTerm) {
            ContainerCraftingTerm craftingTerm = (ContainerCraftingTerm) c;

            GuiCraftingTerm g = (GuiCraftingTerm) craftingTerm.getGui();
            IItemList<IAEItemStack> ir = g.getRepo().getList();
            boolean found;
            boolean craftable;
            this.errored = false;

            for (IGuiIngredient<?> i : recipeLayout.getItemStacks().getGuiIngredients().values()) {
                found = false;
                craftable = false;
                if (i.isInput() && i.getAllIngredients().size() > 0) {
                    for (Object o : i.getAllIngredients()) {
                        if (o instanceof ItemStack) {
                            ItemStack stack = (ItemStack) o;
                            if (!stack.isEmpty()) {
                                if (stack.getItem().isDamageable() || Platform.isGTDamageableItem(stack.getItem())) {
                                    Collection<IAEItemStack> fuzzy = ir.findFuzzy(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL);
                                    if (fuzzy.size() > 0) {
                                        for (IAEItemStack itemStack : fuzzy) {
                                            if (itemStack.getStackSize() > 0) {
                                                if (itemStack.fuzzyComparison(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL)) {
                                                    found = true;
                                                    break;
                                                }
                                            } else {
                                                if (itemStack.isCraftable()) {
                                                    craftable = true;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    IAEItemStack ext = ir.findPrecise(AEItemStack.fromItemStack(stack));
                                    if (ext != null) {
                                        if (ext.getStackSize() > 0) {
                                            break;
                                        } else {
                                            if (ext.isCraftable()) {
                                                craftable = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!found) {
                            if (craftable) {
                                i.drawHighlight(minecraft, new Color(0.0f, 0.0f, 1.0f, 0.4f), recipeX, recipeY);
                            } else {
                                i.drawHighlight(minecraft, new Color(1.0f, 0.0f, 0.0f, 0.4f), recipeX, recipeY);
                            }
                            this.errored = true;
                        } else {
                            this.errored = false;
                        }
                    }
                }
            }
            if (!this.errored) {
                ((RecipeLayout) recipeLayout).getRecipeTransferButton().init(Minecraft.getMinecraft().player.openContainer, Minecraft.getMinecraft().player);
            }
        }
    }

    public boolean errored() {
        return this.errored;
    }
}
