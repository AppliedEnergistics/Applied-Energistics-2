package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.style.ResolvedTextStyle;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class LineTextRun extends LineElement {
    final String text;
    final ResolvedTextStyle style;

    public LineTextRun(String text, ResolvedTextStyle style) {
        this.text = text;
        this.style = style;
    }

    @Override
    public void render(RenderContext context) {
        var matrix = context.poseStack().last().pose();

        var effectiveStyle = Style.EMPTY
                .withBold(style.bold())
                .withItalic(style.italic())
                .withUnderlined(style.underlined())
                .withStrikethrough(style.strikethrough())
                .withFont(style.font());

        float xoff = bounds.x();
        float yoff = bounds.y();
        if (style.fontScale() != 1) {
            matrix = matrix.copy();

            matrix.multiply(style.fontScale());
            matrix.translate(new Vector3f(xoff, yoff, 0));
            xoff = 0;
            yoff = 0;
        }

        context.font().drawInBatch(Component.literal(text).withStyle(effectiveStyle), xoff, yoff, context.resolveColor(style.color()), false,
                matrix, context.multiBufferSource(), false, 0, LightTexture.FULL_BRIGHT);

    }
}
