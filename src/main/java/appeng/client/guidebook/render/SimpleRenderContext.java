package appeng.client.guidebook.render;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.screen.GuideScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;

public record SimpleRenderContext(@Override GuideScreen screen,
                                  @Override LytRect viewport,
                                  PoseStack poseStack,
                                  MultiBufferSource multiBufferSource,
                                  LightDarkMode lightDarkMode) implements RenderContext {

    @Override
    public int resolveColor(ColorRef ref) {
        if (ref.symbolic != null) {
            return ref.symbolic.resolve(lightDarkMode);
        } else {
            return ref.concrete;
        }
    }

    @Override
    public void fillRect(LytRect rect, ColorRef color) {
        GuiComponent.fill(poseStack, rect.x(), rect.y(), rect.right(), rect.bottom(), resolveColor(color));
    }
}
