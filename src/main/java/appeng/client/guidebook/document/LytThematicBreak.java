package appeng.client.guidebook.document;

import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;
import appeng.client.guidebook.render.SymbolicColor;
import net.minecraft.client.gui.GuiComponent;

public class LytThematicBreak extends LytBlock {
    @Override
    public LytRect computeLayout(LayoutContext context) {
        return context.available().withHeight(6);
    }

    @Override
    public void render(SimpleRenderContext context) {
        var line = bounds.withHeight(2).centerVerticallyIn(bounds);

        context.fillRect(line, SymbolicColor.THEMATIC_BREAK.ref());
    }
}
