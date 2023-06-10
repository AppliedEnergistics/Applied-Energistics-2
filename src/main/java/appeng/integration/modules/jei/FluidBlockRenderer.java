package appeng.integration.modules.jei;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientRenderer;

import appeng.integration.modules.jeirei.FluidBlockRendering;

public class FluidBlockRenderer implements IIngredientRenderer<IJeiFluidIngredient> {
    @Override
    public void render(GuiGraphics guiGraphics, IJeiFluidIngredient ingredient) {
        var fluid = ingredient.getFluid();
        FluidBlockRendering.render(guiGraphics, fluid, 0, 0, 16, 16);
    }

    @Override
    public List<Component> getTooltip(IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
        FluidVariant fluidVariant = FluidVariant.of(ingredient.getFluid());
        return FluidVariantRendering.getTooltip(fluidVariant, tooltipFlag);
    }
}
