package appeng.client.integration.rei;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;

import appeng.api.stacks.AEFluidKey;
import appeng.client.api.AEKeyRendering;
import appeng.client.integration.itemlists.FluidBlockRendering;

public class FluidBlockRenderer implements EntryRenderer<FluidStack> {
    @Override
    public void render(EntryStack<FluidStack> entry, GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY,
            float delta) {
        var fluid = entry.getValue().getFluid();

        FluidBlockRendering.render(guiGraphics, fluid, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, TooltipContext context) {
        var key = AEFluidKey.of(FluidStackHooksForge.toForge(entry.getValue()));
        return Tooltip.create(context.getPoint(), AEKeyRendering.getTooltip(key));
    }
}
