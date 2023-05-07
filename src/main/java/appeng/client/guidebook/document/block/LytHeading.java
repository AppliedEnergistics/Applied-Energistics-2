package appeng.client.guidebook.document.block;

import net.minecraft.client.Minecraft;

import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.style.TextStyle;

public class LytHeading extends LytParagraph {
    private int depth = 1;

    public LytHeading() {
        setMarginTop(5);
        setMarginBottom(5);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        modifyStyle(this::customizeStyle);
    }

    private void customizeStyle(TextStyle.Builder builder) {
        switch (depth) {
            case 1 -> builder.fontScale(1.3f).bold(true).font(Minecraft.DEFAULT_FONT).color(ColorRef.WHITE);
            case 2 -> builder.fontScale(1.1f).font(Minecraft.DEFAULT_FONT);
            case 3 -> builder.fontScale(1f).font(Minecraft.DEFAULT_FONT);
            case 4 -> builder.fontScale(1.1f).bold(true).font(Minecraft.UNIFORM_FONT);
            case 5 -> builder.fontScale(1f).bold(true).font(Minecraft.UNIFORM_FONT);
            case 6 -> builder.fontScale(1f).font(Minecraft.UNIFORM_FONT);
            default -> {
            }
        }
    }

    @Override
    public void render(RenderContext context) {
        super.render(context);

        if (depth == 1) {
            var bounds = getBounds();
            context.fillRect(
                    bounds.x(), bounds.bottom() - 1, bounds.width(), 1, SymbolicColor.HEADER1_SEPARATOR.ref());
        } else if (depth == 2) {
            var bounds = getBounds();
            context.fillRect(
                    bounds.x(), bounds.bottom() - 1, bounds.width(), 1, SymbolicColor.HEADER2_SEPARATOR.ref());
        }
    }
}
