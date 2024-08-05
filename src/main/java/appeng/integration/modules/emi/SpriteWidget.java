package appeng.integration.modules.emi;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;

final class SpriteWidget extends Widget {
    private final ResourceLocation id;
    private final Bounds bounds;

    SpriteWidget(ResourceLocation id, int x, int y, int width, int height) {
        this.id = id;
        this.bounds = new Bounds(x, y, width, height);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        draw.blitSprite(id, bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }
}
