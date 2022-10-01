package appeng.integration.modules.jei.widgets;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

public interface Widget {
    void draw(PoseStack stack);

    default boolean hitTest(double x, double y) {
        return false;
    }

    default List<Component> getTooltipLines() {
        return List.of();
    }
}
