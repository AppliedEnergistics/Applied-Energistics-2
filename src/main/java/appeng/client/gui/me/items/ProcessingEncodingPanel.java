package appeng.client.gui.me.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.TextAlignment;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Label;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;

public class ProcessingEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);

    private final ActionButton clearBtn;
    private final ActionButton cycleOutputBtn;
    private final Scrollbar scrollbar;
    private final Label[] rowLabels;

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

        this.rowLabels = new Label[3];

        for (var i = 0; i < this.rowLabels.length; i += 1) {
            var label = new Label(Component.empty(), font);
            label
                    .setDropShadow(false)
                    .setAlignX(TextAlignment.RIGHT)
                    .setAlignY(TextAlignment.CENTER)
                    .setColor(0x696D88);

            widgets.add(String.format("rowLabel%d", i), label);

            this.rowLabels[i] = label;
        }
    }

    @Override
    public void updateBeforeRender() {
        // Update the processing row position/visibility
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

        updateTooltipVisibility();

        var row = scrollbar.getCurrentScroll() + 1;
        for (var label : rowLabels) {
            label.setMessage(Component.literal(Integer.toString(row)));
            row += 1;
        }
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

        for (var label : rowLabels)
            label.visible = visible;

        updateTooltipVisibility();
    }
}
