package appeng.integration.modules.rei;

import net.minecraft.client.gui.GuiGraphics;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;

import appeng.client.gui.style.BackgroundGenerator;

public record BackgroundRenderer(int width, int height) implements Renderer {

    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        BackgroundGenerator.draw(width, height, graphics, bounds.x, bounds.y);
    }
}
