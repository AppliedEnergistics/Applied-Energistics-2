package appeng.integration.modules.jei;

import appeng.container.implementations.ContainerPatternTerm;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;

import javax.annotation.Nonnull;
import java.awt.*;

import static mezz.jei.api.recipe.transfer.IRecipeTransferError.Type.USER_FACING;

public class JEIMissingItem implements IRecipeTransferError {

    @Nonnull
    @Override
    public Type getType() {
        return USER_FACING;
    }

    @Override
    public void showError(Minecraft minecraft, int mouseX, int mouseY, @Nonnull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
        Container c = minecraft.player.openContainer;
        if (c instanceof ContainerPatternTerm) {
            for (IGuiIngredient<?> i : recipeLayout.getItemStacks().getGuiIngredients().values()) {
                i.drawHighlight(minecraft, new Color(1.0f, 0.0f, 0.0f, 0.4f), recipeX, recipeY);
            }
        }
    }
}
