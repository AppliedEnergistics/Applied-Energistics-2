package appeng.integration.modules.jei;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.channels.IItemStorageChannel;
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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static mezz.jei.api.recipe.transfer.IRecipeTransferError.Type.USER_FACING;

public class JEIMissingItem implements IRecipeTransferError {

    private boolean errored;

    JEIMissingItem(Container container, @Nonnull IRecipeLayout recipeLayout) {
        if (container instanceof ContainerCraftingTerm) {
            ContainerCraftingTerm craftingTerm = (ContainerCraftingTerm) container;
            GuiCraftingTerm g = (GuiCraftingTerm) craftingTerm.getGui();

            IItemList<IAEItemStack> ir = g.getRepo().getList();
            IItemList<IAEItemStack> playerInv = wrapPlayerInv(((ContainerCraftingTerm) container).getPlayerInv());

            IItemList<IAEItemStack> available = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
            for (IAEItemStack i : ir) {
                available.addStorage(i);
            }
            for (IAEItemStack i : playerInv) {
                available.addStorage(i);
            }
            boolean found;
            this.errored = false;
            recipeLayout.getItemStacks().addTooltipCallback(new CraftableCallBack(container, available));

            IItemList<IAEItemStack> used = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
            for (IGuiIngredient<?> i : recipeLayout.getItemStacks().getGuiIngredients().values()) {
                found = false;
                if (i.isInput() && i.getAllIngredients().size() > 0) {
                    List<?> allIngredients = i.getAllIngredients();
                    for (Object allIngredient : allIngredients) {
                        if (allIngredient instanceof ItemStack) {
                            ItemStack stack = (ItemStack) allIngredient;
                            if (!stack.isEmpty()) {
                                if (stack.getItem().isDamageable() || Platform.isGTDamageableItem(stack.getItem())) {
                                    Collection<IAEItemStack> fuzzy = available.findFuzzy(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL);
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
                                    IAEItemStack ext = available.findPrecise(AEItemStack.fromItemStack(stack));
                                    if (ext != null) {
                                        IAEItemStack usedStack = used.findPrecise(ext);
                                        if (ext.getStackSize() > 0 && (usedStack == null || ext.getStackSize() > usedStack.getStackSize())) {
                                            used.add(ext.copy().setStackSize(1));
                                            found = true;
                                        } else {
                                            found = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!found) {
                        this.errored = true;
                        break;
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
            IItemList<IAEItemStack> playerInv = wrapPlayerInv(minecraft.player.inventory);
            boolean found;
            boolean craftable;
            int currentSlot = 0;
            this.errored = false;
            IItemList<IAEItemStack> available = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
            for (IAEItemStack i : ir) {
                available.addStorage(i);
            }
            for (IAEItemStack i : playerInv) {
                available.addStorage(i);
            }
            IItemList<IAEItemStack> used = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

            for (IGuiIngredient<?> i : recipeLayout.getItemStacks().getGuiIngredients().values()) {
                found = false;
                craftable = false;
                List<ItemStack> valid = new ArrayList<>();
                if (i.isInput() && i.getAllIngredients().size() > 0) {
                    List<?> allIngredients = i.getAllIngredients();
                    for (Object allIngredient : allIngredients) {
                        if (allIngredient instanceof ItemStack) {
                            ItemStack stack = (ItemStack) allIngredient;
                            if (!stack.isEmpty()) {
                                if (stack.getItem().isDamageable() || Platform.isGTDamageableItem(stack.getItem())) {
                                    Collection<IAEItemStack> fuzzy = available.findFuzzy(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL);
                                    if (fuzzy.size() > 0) {
                                        for (IAEItemStack itemStack : fuzzy) {
                                            if (itemStack.getStackSize() > 0) {
                                                if (itemStack.fuzzyComparison(AEItemStack.fromItemStack(stack), FuzzyMode.IGNORE_ALL)) {
                                                    found = true;
                                                    used.add(itemStack.copy().setStackSize(1));
                                                    valid.addAll(fuzzy.stream().map(IAEItemStack::createItemStack).collect(Collectors.toList()));
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
                                    IAEItemStack ext = available.findPrecise(AEItemStack.fromItemStack(stack));
                                    if (ext != null) {
                                        IAEItemStack usedStack = used.findPrecise(ext);
                                        if (ext.getStackSize() > 0 && (usedStack == null || usedStack.getStackSize() < ext.getStackSize())) {
                                            used.add(ext.copy().setStackSize(1));
                                            if (craftable) {
                                                valid.clear();
                                            }
                                            valid.add(ext.copy().setStackSize(1).createItemStack());
                                            found = true;
                                        } else if (ext.isCraftable()) {
                                            valid.add(ext.copy().setStackSize(1).createItemStack());
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
                            recipeLayout.getItemStacks().set(currentSlot, valid);
                        } else {
                            i.drawHighlight(minecraft, new Color(1.0f, 0.0f, 0.0f, 0.4f), recipeX, recipeY);
                        }
                        this.errored = true;
                    } else {
                        recipeLayout.getItemStacks().set(currentSlot, valid);
                        this.errored = false;
                    }
                }
                currentSlot++;
            }
            if (!this.errored) {
                ((RecipeLayout) recipeLayout).getRecipeTransferButton().init(Minecraft.getMinecraft().player.openContainer, Minecraft.getMinecraft().player);
            }
        }
    }

    public boolean errored() {
        return this.errored;
    }


    IItemList<IAEItemStack> wrapPlayerInv(InventoryPlayer inventory) {
        IItemList<IAEItemStack> used = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

        PlayerMainInvWrapper invWrapper = new PlayerMainInvWrapper(inventory);
        for (int i = 0; i < invWrapper.getSlots(); i++) {
            used.addStorage(AEItemStack.fromItemStack(invWrapper.getStackInSlot(i)));
        }

        return used;
    }
}
