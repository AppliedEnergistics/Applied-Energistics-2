package appeng.client.gui.widgets;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.localization.GuiText;

public class PatternBoxPanel implements ICompositeWidget {

    private final Blitter background;
    private final Component name;

    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public PatternBoxPanel(ScreenStyle style, Component name) {
        this.background = style.getImage("patternbox");
        this.name = name;
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        background.dest(
                bounds.getX() + this.bounds.getX(),
                bounds.getY() + this.bounds.getY(),
                this.bounds.getWidth(),
                this.bounds.getHeight()).blit(guiGraphics);
    }

    @Override
    public @Nullable Tooltip getTooltip(int mouseX, int mouseY) {
        return new Tooltip(
                this.name,
                GuiText.PatternStorage.text().plainCopy().withStyle(ChatFormatting.GRAY));
    }
}
