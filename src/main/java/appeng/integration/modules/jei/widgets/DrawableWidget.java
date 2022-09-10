package appeng.integration.modules.jei.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.drawable.IDrawable;

public class DrawableWidget implements Widget {
    private final IDrawable drawable;
    private final int x;
    private final int y;

    public DrawableWidget(IDrawable drawable, int x, int y) {
        this.drawable = drawable;
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(PoseStack stack) {
        drawable.draw(stack, x, y);
    }
}
