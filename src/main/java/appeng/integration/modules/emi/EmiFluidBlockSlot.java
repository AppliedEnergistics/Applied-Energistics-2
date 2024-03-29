package appeng.integration.modules.emi;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;

import appeng.integration.modules.itemlists.FluidBlockRendering;

class EmiFluidBlockSlot extends SlotWidget {

    private final List<Fluid> allFluids;

    public EmiFluidBlockSlot(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
        allFluids = stack.getEmiStacks()
                .stream()
                .map(s -> s.getKeyOfType(Fluid.class))
                .distinct()
                .toList();
    }

    @Override
    public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Bounds bounds = getBounds();
        FluidBlockRendering.render(draw, getCurrentFluid(), bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    private Fluid getCurrentFluid() {
        if (allFluids.isEmpty()) {
            return Fluids.EMPTY;
        }
        int item = (int) (System.currentTimeMillis() / 1000 % allFluids.size());
        return allFluids.get(item);
    }
}
