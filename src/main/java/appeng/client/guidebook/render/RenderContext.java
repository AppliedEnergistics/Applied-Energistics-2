package appeng.client.guidebook.render;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.screen.GuideScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;

public interface RenderContext {

    GuideScreen screen();

    PoseStack poseStack();

    LytRect viewport();

    MultiBufferSource multiBufferSource();

    void fillRect(LytRect rect, ColorRef color);

    int resolveColor(ColorRef ref);

    default Font font() {
        return Minecraft.getInstance().font;
    }

    default void fillRect(int x, int y, int width, int height, ColorRef color) {
        fillRect(new LytRect(x, y, width, height), color);
    }

}
