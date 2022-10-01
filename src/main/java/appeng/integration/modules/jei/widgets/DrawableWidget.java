package appeng.integration.modules.jei.widgets;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.drawable.IDrawable;

public class DrawableWidget extends AbstractWidget {
    private final IDrawable drawable;
    private final int x;
    private final int y;

    public DrawableWidget(IDrawable drawable, int x, int y) {
        this.drawable = drawable;
        this.x = x;
        this.y = y;
    }

    public DrawableWidget tooltip(Component component) {
        setTooltipLines(List.of(component));
        return this;
    }

    @Override
    public boolean hitTest(double x, double y) {
        return x >= this.x && x < this.x + drawable.getWidth()
                && y >= this.y && y < this.y + drawable.getHeight();
    }

    @Override
    public void draw(PoseStack stack) {
        drawable.draw(stack, x, y);
    }
}
