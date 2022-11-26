package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.LytSize;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.layout.SimpleLayoutContext;
import appeng.client.guidebook.screen.GuideScreen;
import net.minecraft.client.Minecraft;

import java.util.Optional;

public class LytFlowInlineBlock extends LytFlowContent implements InteractiveElement {

    private LytBlock block;

    public LytBlock getBlock() {
        return block;
    }

    public void setBlock(LytBlock block) {
        this.block = block;
    }

    public LytSize getPreferredSize(int lineWidth) {
        if (block == null) {
            return LytSize.empty();
        }

        // We need to compute the layout
        var layoutContext = new SimpleLayoutContext(Minecraft.getInstance().font, LytRect.empty());
        var bounds = block.layout(layoutContext, 0, 0, lineWidth);
        return new LytSize(bounds.right(), bounds.bottom());
    }

    @Override
    public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        if (block instanceof InteractiveElement interactiveElement) {
            return interactiveElement.mouseClicked(screen, x, y, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(GuideScreen screen, int x, int y, int button) {
        if (block instanceof InteractiveElement interactiveElement) {
            return interactiveElement.mouseReleased(screen, x, y, button);
        }
        return false;
    }

    @Override
    public Optional<GuideTooltip> getTooltip() {
        if (block instanceof InteractiveElement interactiveElement) {
            return interactiveElement.getTooltip();
        }
        return Optional.empty();
    }
}
