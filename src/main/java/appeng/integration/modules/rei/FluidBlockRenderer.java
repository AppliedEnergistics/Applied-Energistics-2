package appeng.integration.modules.rei;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;

import appeng.api.client.AEStackRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.integration.modules.jeirei.FluidBlockRendering;

public class FluidBlockRenderer implements EntryRenderer<FluidStack> {
    @Override
    public void render(EntryStack<FluidStack> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY,
            float delta) {
        var fluid = entry.getValue().getFluid();

        FluidBlockRendering.render(matrices, fluid, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        var key = AEFluidKey.of(entry.getValue().getFluid(), entry.getValue().getTag());
        return Tooltip.create(context.getPoint(), AEStackRendering.getTooltip(key));
    }
}
