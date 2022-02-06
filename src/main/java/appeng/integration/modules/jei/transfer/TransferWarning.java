package appeng.integration.modules.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

record TransferWarning(IRecipeTransferError parent) implements IRecipeTransferError {
    @Override
    public Type getType() {
        return Type.COSMETIC;
    }

    @Override
    public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
            int recipeY) {
        this.parent.showError(poseStack, mouseX, mouseY, recipeLayout, recipeX, recipeY);
    }
}
