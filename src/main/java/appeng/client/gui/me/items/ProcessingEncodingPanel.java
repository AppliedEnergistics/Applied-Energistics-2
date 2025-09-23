package appeng.client.gui.me.items;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.TextureTransform;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;

public class ProcessingEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);

    private final ActionButton clearBtn;
    private final ActionButton cycleInputForwardBtn;
    private final ActionButton cycleInputBackwardBtn;
    private final ActionButton cycleOutputForwardBtn;
    private final ActionButton cycleOutputBackwardBtn;
    private final Scrollbar scrollbar;

    public ProcessingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);

        // Add buttons for the processing mode
        clearBtn = new ActionButton(ActionItems.S_CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        clearBtn.setDisableBackground(true);
        widgets.add("processingClearPattern", clearBtn);

        this.cycleInputForwardBtn = new ActionButton(
                ActionItems.S_CYCLE_PROCESSING_INPUT_FORWARD,
                act -> menu.cycleProcessingInputForward());
        this.cycleInputForwardBtn.setHalfSize(true);
        this.cycleInputForwardBtn.setDisableBackground(true);
        widgets.add("processingCycleInputForward", this.cycleInputForwardBtn);

        this.cycleInputBackwardBtn = new ActionButton(
                ActionItems.S_CYCLE_PROCESSING_INPUT_BACKWARD,
                act -> menu.cycleProcessingInputBackward());
        this.cycleInputBackwardBtn.setHalfSize(true);
        this.cycleInputBackwardBtn.setDisableBackground(true);
        this.cycleInputBackwardBtn.setTransform(TextureTransform.MIRROR_H);
        widgets.add("processingCycleInputBackward", this.cycleInputBackwardBtn);

        this.cycleOutputForwardBtn = new ActionButton(
                ActionItems.S_CYCLE_PROCESSING_OUTPUT_FORWARD,
                act -> menu.cycleProcessingOutputForward());
        this.cycleOutputForwardBtn.setHalfSize(true);
        this.cycleOutputForwardBtn.setDisableBackground(true);
        widgets.add("processingCycleOutputForward", this.cycleOutputForwardBtn);

        this.cycleOutputBackwardBtn = new ActionButton(
                ActionItems.S_CYCLE_PROCESSING_OUTPUT_BACKWARD,
                act -> menu.cycleProcessingOutputBackward());
        this.cycleOutputBackwardBtn.setHalfSize(true);
        this.cycleOutputBackwardBtn.setDisableBackground(true);
        this.cycleOutputBackwardBtn.setTransform(TextureTransform.MIRROR_H);
        widgets.add("processingCycleOutputBackward", this.cycleOutputBackwardBtn);

        this.scrollbar = widgets.addScrollBar("processingPatternModeScrollbar", Scrollbar.SMALL);
        // The scrollbar ranges from 0 to the number of rows not visible
        this.scrollbar.setRange(0, menu.getProcessingInputSlots().length / 3 - 3, 3);
        this.scrollbar.setCaptureMouseWheel(false);

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
        clearBtn.setVisibility(menu.canClear());
        cycleOutputForwardBtn.setVisibility(menu.canCycleProcessingOutputs() && !Screen.hasShiftDown());
        cycleOutputBackwardBtn.setVisibility(menu.canCycleProcessingOutputs() && Screen.hasShiftDown());
        cycleInputForwardBtn.setVisibility(menu.canCycleProcessingInputs() && !Screen.hasShiftDown());
        cycleInputBackwardBtn.setVisibility(menu.canCycleProcessingInputs() && Screen.hasShiftDown());

        screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
        screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !visible);

        updateTooltipVisibility();
    }
}
