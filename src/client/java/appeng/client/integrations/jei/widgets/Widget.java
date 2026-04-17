package appeng.client.integrations.jei.widgets;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface Widget {
    void draw(GuiGraphicsExtractor guiGraphics);

    default boolean hitTest(double x, double y) {
        return false;
    }

    default List<Component> getTooltipLines() {
        return List.of();
    }
}
