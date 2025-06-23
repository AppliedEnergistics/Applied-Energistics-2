package appeng.client.gui.me.items;

import appeng.client.gui.style.TextAlignment;
import appeng.client.gui.widgets.Label;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;

public class ProcessingEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);

    private final ActionButton clearBtn;
    private final ActionButton cycleOutputBtn;
    private final Scrollbar scrollbar;
    private final Label[] inputLabels;

    public ProcessingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);

        // Add buttons for the processing mode
        clearBtn = new ActionButton(ActionItems.S_CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        clearBtn.setDisableBackground(true);
        widgets.add("processingClearPattern", clearBtn);

        this.cycleOutputBtn = new ActionButton(
                ActionItems.S_CYCLE_PROCESSING_OUTPUT,
                act -> menu.cycleProcessingOutput());
        this.cycleOutputBtn.setHalfSize(true);
        this.cycleOutputBtn.setDisableBackground(true);
        widgets.add("processingCycleOutput", this.cycleOutputBtn);

        this.scrollbar = widgets.addScrollBar("processingPatternModeScrollbar", Scrollbar.SMALL);
        // The scrollbar ranges from 0 to the number of rows not visible
        this.scrollbar.setRange(0, menu.getProcessingInputSlots().length / 3 - 3, 3);
        this.scrollbar.setCaptureMouseWheel(false);

        var font = Minecraft.getInstance().font;

        this.inputLabels = new Label[9];

        var aligns = new TextAlignment[] {
            TextAlignment.LEFT,
            TextAlignment.CENTER,
            TextAlignment.RIGHT,
        };

        var i = 0;
        for (var vert: aligns)
            for (var horiz: aligns) {
                var label = new Label(Component.empty(), font);
                var hLetter = switch(horiz) {
                    case LEFT -> "L";
                    case CENTER -> "C";
                    case RIGHT -> "R";
                };
                var vLetter = switch(vert) {
                    case LEFT -> "T";
                    case CENTER -> "C";
                    case RIGHT -> "B";
                };
                label
                    .setDropShadow(false)
                    .setAlignX(horiz)
                    .setAlignY(vert);

                widgets.add(String.format("slotLabel%s%s", hLetter, vLetter), label);

                this.inputLabels[i] = label;
                i += 1;
        }
    }

    @Override
    public void updateBeforeRender() {
        // Update the processing slot position/visibility
        screen.repositionSlots(SlotSemantics.PROCESSING_INPUTS);
        screen.repositionSlots(SlotSemantics.PROCESSING_OUTPUTS);

        for (int i = 0; i < menu.getProcessingInputSlots().length; i++) {
            var slot = menu.getProcessingInputSlots()[i];
            var effectiveRow = (i / 3) - scrollbar.getCurrentScroll();

            slot.setActive(effectiveRow >= 0 && effectiveRow < 3);
            slot.y -= scrollbar.getCurrentScroll() * 18;
        }
        for (int i = 0; i < menu.getProcessingOutputSlots().length; i++) {
            var slot = menu.getProcessingOutputSlots()[i];
            var effectiveRow = i - scrollbar.getCurrentScroll();

            slot.setActive(effectiveRow >= 0 && effectiveRow < 3);
            slot.y -= scrollbar.getCurrentScroll() * 18;
        }

        var slot = scrollbar.getCurrentScroll() * 3;
        for (var label: inputLabels) {
            label.setMessage(Component.literal(Integer.toString(slot)));
            slot += 1;
        }

        updateTooltipVisibility();
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 8, bounds.getY() + bounds.getHeight() - 165).blit(guiGraphics);
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        return scrollbar.onMouseWheel(mousePos, delta);
    }

    private void updateTooltipVisibility() {
        widgets.setTooltipAreaEnabled("processing-primary-output", visible && scrollbar.getCurrentScroll() == 0);
        widgets.setTooltipAreaEnabled("processing-optional-output1", visible && scrollbar.getCurrentScroll() > 0);
        widgets.setTooltipAreaEnabled("processing-optional-output2", visible);
        widgets.setTooltipAreaEnabled("processing-optional-output3", visible);
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_PROCESSING;
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.ProcessingPattern.text();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        scrollbar.setVisible(visible);
        clearBtn.setVisibility(visible);
        cycleOutputBtn.setVisibility(menu.canCycleProcessingOutputs());

        screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
        screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !visible);

        updateTooltipVisibility();
    }
}
