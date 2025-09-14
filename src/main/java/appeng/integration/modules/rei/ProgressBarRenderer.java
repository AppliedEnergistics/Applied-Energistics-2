package appeng.integration.modules.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record ProgressBarRenderer(ResourceLocation location, int x, int y, int width, int height, int u, int v) implements Renderer {
    private static final int ANIMATION_TIME = 2000;
    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        int subTime = (int) (System.currentTimeMillis() % ANIMATION_TIME);
        subTime = ANIMATION_TIME - subTime;
        int my = y + height * subTime / ANIMATION_TIME;
        int mv = v + height * subTime / ANIMATION_TIME;
        int mHeight = height - (my - y);
        graphics.blit(location, x, my, width, mHeight, u, mv, width, mHeight, 256, 256);
    }
}
