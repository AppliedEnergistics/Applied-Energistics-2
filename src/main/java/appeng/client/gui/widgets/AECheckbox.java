package appeng.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;

public class AECheckbox extends AbstractButton {
    private static final int SIZE = 14;

    private static final Blitter BLITTER = Blitter.texture("guis/checkbox.png", 64, 64);

    private static final Blitter UNCHECKED = BLITTER.copy().src(0, 0, SIZE, SIZE);
    private static final Blitter UNCHECKED_FOCUS = BLITTER.copy().src(SIZE, 0, SIZE, SIZE);
    private static final Blitter CHECKED = BLITTER.copy().src(0, SIZE, SIZE, SIZE);
    private static final Blitter CHECKED_FOCUS = BLITTER.copy().src(SIZE, SIZE, SIZE, SIZE);

    private static final Blitter RADIO_UNCHECKED = BLITTER.copy().src(2 * SIZE, 0, SIZE, SIZE);
    private static final Blitter RADIO_UNCHECKED_FOCUS = BLITTER.copy().src(3 * SIZE, 0, SIZE, SIZE);
    private static final Blitter RADIO_CHECKED = BLITTER.copy().src(2 * SIZE, SIZE, SIZE, SIZE);
    private static final Blitter RADIO_CHECKED_FOCUS = BLITTER.copy().src(3 * SIZE, SIZE, SIZE, SIZE);

    private final ScreenStyle style;
    private boolean selected;
    private Runnable changeListener;
    private boolean radio;

    public AECheckbox(int x, int y, int width, int height, ScreenStyle style, Component component) {
        super(x, y, width, height, component);
        this.style = style;
    }

    @Override
    public void onPress() {
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
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
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Blitter icon;
        if (isRadio()) {
            if (isFocused() || isMouseOver(mouseX, mouseY)) {
                icon = isSelected() ? RADIO_CHECKED_FOCUS : RADIO_UNCHECKED_FOCUS;
            } else {
                icon = isSelected() ? RADIO_CHECKED : RADIO_UNCHECKED;
            }
        } else {
            if (isFocused() || isMouseOver(mouseX, mouseY)) {
                icon = isSelected() ? CHECKED_FOCUS : UNCHECKED_FOCUS;
            } else {
                icon = isSelected() ? CHECKED : UNCHECKED;
            }
        }

        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        var textColor = isActive() ? PaletteColor.DEFAULT_TEXT_COLOR : PaletteColor.MUTED_TEXT_COLOR;
        var opacity = isActive() ? 1 : 0.5f;

        icon.dest(x, y).opacity(opacity).blit(poseStack, getBlitOffset());
        font.draw(poseStack, getMessage(), x + SIZE + 2, y + 4, style.getColor(textColor).toARGB());
    }
}
