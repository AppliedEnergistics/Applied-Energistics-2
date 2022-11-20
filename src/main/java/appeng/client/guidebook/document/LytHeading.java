package appeng.client.guidebook.document;

import appeng.client.guidebook.document.flow.LytFlowContainer;
import net.minecraft.client.Minecraft;

public class LytHeading extends LytParagraph {
    public LytHeading() {
        setMarginTop(5);
        setMarginBottom(5);
    }

    private int depth;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        var fontScale = Math.max(1, 1.75f - depth * 0.25f);
        content.getRootSpan().modifyStyle(builder -> builder.fontScale(fontScale).font(Minecraft.DEFAULT_FONT));
    }
}
