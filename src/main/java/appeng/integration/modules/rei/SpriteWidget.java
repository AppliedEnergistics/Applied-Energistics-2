package appeng.integration.modules.rei;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;

final class SpriteWidget extends WidgetWithBounds {
    private final ResourceLocation id;
    private final Rectangle bounds;

    SpriteWidget(ResourceLocation id, int x, int y, int width, int height) {
        this.id = id;
        this.bounds = new Rectangle(x, y, width, height);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(id, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
