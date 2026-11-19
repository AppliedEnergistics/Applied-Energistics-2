package appeng.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SpriteRenderer;
import appeng.core.AppEng;

public class AECheckbox extends AbstractButton {
    public static final int SIZE = 14;

    private static final Identifier UNCHECKED = AppEng.makeId("checkbox/unchecked");
    private static final Identifier UNCHECKED_FOCUS = AppEng.makeId("checkbox/unchecked_focus");
    private static final Identifier CHECKED = AppEng.makeId("checkbox/checked");
    private static final Identifier CHECKED_FOCUS = AppEng.makeId("checkbox/checked_focus");
    private static final Identifier RADIO_UNCHECKED = AppEng.makeId("checkbox/radio_unchecked");
    private static final Identifier RADIO_UNCHECKED_FOCUS = AppEng.makeId("checkbox/radio_unchecked_focus");
    private static final Identifier RADIO_CHECKED = AppEng.makeId("checkbox/radio_checked");
    private static final Identifier RADIO_CHECKED_FOCUS = AppEng.makeId("checkbox/radio_checked_focus");
    private final ScreenStyle style;
    private boolean selected;
    private Runnable changeListener;
    private boolean radio;

    public AECheckbox(int x, int y, int width, int height, ScreenStyle style, Component component) {
        super(x, y, width, height, component);
        this.style = style;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.selected = !this.selected;
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    public boolean isRadio() {
        return radio;
    }

    public void setRadio(boolean radio) {
        this.radio = radio;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE,
                        Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE,
                        Component.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Identifier icon;
        if (isRadio()) {
            if (isMouseOver(mouseX, mouseY)) {
                icon = isSelected() ? RADIO_CHECKED_FOCUS : RADIO_UNCHECKED_FOCUS;
            } else {
                icon = isSelected() ? RADIO_CHECKED : RADIO_UNCHECKED;
            }
        } else {
            if (isMouseOver(mouseX, mouseY) && !isFocused()) {
                icon = isSelected() ? CHECKED_FOCUS : UNCHECKED_FOCUS;
            } else {
                icon = isSelected() ? CHECKED : UNCHECKED;
            }
        }

        if (!isMouseOver(mouseX, mouseY)) {
            setFocused(false);
        }

        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        var textColor = isActive() ? PaletteColor.DEFAULT_TEXT_COLOR : PaletteColor.MUTED_TEXT_COLOR;
        var opacity = isActive() ? 1 : 0.5f;

        SpriteRenderer.of(guiGraphics).blitSprite(icon, getX(), getY(), ARGB.white(opacity));
        var lines = font.split(getMessage(), width - 22);
        // try to vertically center if it's just one line
        var lineY = getY() + (lines.size() <= 1 ? 4 : 1);
        for (var line : lines) {
            guiGraphics.text(font, line, getX() + (isRadio() ? 16 : 26), lineY,
                    style.getColor(textColor).toARGB(), false);
            lineY += font.lineHeight;
        }
    }
}
