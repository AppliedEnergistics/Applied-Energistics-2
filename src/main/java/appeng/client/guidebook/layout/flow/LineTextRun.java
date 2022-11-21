package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.style.ResolvedTextStyle;
import net.minecraft.client.renderer.MultiBufferSource;

public class LineTextRun extends LineElement {
    final String text;
    final ResolvedTextStyle style;
    final ResolvedTextStyle hoverStyle;

    public LineTextRun(String text, ResolvedTextStyle style, ResolvedTextStyle hoverStyle) {
        this.text = text;
        this.style = style;
        this.hoverStyle = hoverStyle;
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        var style = containsMouse ? this.hoverStyle : this.style;

        context.renderTextInBatch(text, style, (float) bounds.x(), (float) bounds.y(), buffers);
    }
}
