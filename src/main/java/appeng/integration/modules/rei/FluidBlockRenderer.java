package appeng.integration.modules.rei;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.plugin.client.entry.FluidEntryDefinition;

import appeng.integration.modules.jeirei.FluidBlockRendering;

public class FluidBlockRenderer extends FluidEntryDefinition.FluidEntryRenderer {
    @Override
    public void render(EntryStack<FluidStack> entry, PoseStack matrices, Rectangle rectangle, int mouseX, int mouseY,
            float delta) {
        var fluid = entry.getValue().getFluid();

        FluidBlockRendering.render(matrices, fluid, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
}
