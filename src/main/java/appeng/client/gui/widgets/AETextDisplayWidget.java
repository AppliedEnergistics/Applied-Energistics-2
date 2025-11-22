package appeng.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;

/**
 * A widget that just displays text. Reuses AECheckbox text wrapping code. Useful to have a bit of text in
 * TerminalSettingsScreen for example.
 */
public class AETextDisplayWidget extends AbstractWidget {

    private final ScreenStyle style;

    public AETextDisplayWidget(int x, int y, int width, int height, ScreenStyle style, Component component) {
        super(x, y, width, height, component);
        this.style = style;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
        // Not clickable, no sound
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        var textColor = isActive() ? PaletteColor.DEFAULT_TEXT_COLOR : PaletteColor.MUTED_TEXT_COLOR;
        var opacity = isActive() ? 1 : 0.5f;

        var lines = font.split(getMessage(), width);
        var lineY = getY();
        for (var line : lines) {
            guiGraphics.drawString(font, line, getX(), lineY, style.getColor(textColor).toARGB(), false);
            lineY += font.lineHeight;
        }
    }
}
