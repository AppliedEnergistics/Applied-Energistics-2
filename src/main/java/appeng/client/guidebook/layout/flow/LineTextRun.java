package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.render.RenderContext;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class LineTextRun extends LineElement {
    final String text;
    final Style style;

    public LineTextRun(String text, Style style) {
        this.text = text;
        this.style = style;
    }

    @Override
    public void render(RenderContext context) {
        var matrix = context.poseStack().last().pose();

        context.font().drawInBatch(Component.literal(text).withStyle(style), bounds.x(), bounds.y(), -1, false,
                matrix, context.multiBufferSource(), false, 0, LightTexture.FULL_BRIGHT);

    }
}
