package appeng.client.gui.me.items;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ModifyIcon;
import appeng.client.gui.widgets.ModifyIconButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;

public class ProcessingEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 126, 68);

    private final ActionButton clearBtn;
    private final ActionButton cycleOutputBtn;
    private final Scrollbar scrollbar;

    private final ModifyIconButton multTwo;
    private final ModifyIconButton multThree;
    private final ModifyIconButton multEight;
    private final ModifyIconButton divTwo;
    private final ModifyIconButton divThree;
    private final ModifyIconButton divEight;

    public ProcessingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);

        // Add buttons for the processing mode
        clearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        widgets.add("processingClearPattern", clearBtn);

        this.cycleOutputBtn = new ActionButton(
                ActionItems.CYCLE_PROCESSING_OUTPUT,
                act -> menu.cycleProcessingOutput());
        this.cycleOutputBtn.setHalfSize(true);
        widgets.add("processingCycleOutput", this.cycleOutputBtn);

        this.scrollbar = widgets.addScrollBar("processingPatternModeScrollbar", Scrollbar.SMALL);
        // The scrollbar ranges from 0 to the number of rows not visible
        this.scrollbar.setRange(0, menu.getProcessingInputSlots().length / 3 - 3, 3);
        this.scrollbar.setCaptureMouseWheel(false);

        this.multTwo = new ModifyIconButton(b -> menu.modifyPattern(2),
                ModifyIcon.MULTIPLY_2,
                ButtonToolTips.PatternMultiply.text(2),
                ButtonToolTips.PatternMultiplyHint.text(2));
        this.multThree = new ModifyIconButton(b -> menu.modifyPattern(3),
                ModifyIcon.MULTIPLY_3,
                ButtonToolTips.PatternMultiply.text(3),
                ButtonToolTips.PatternMultiplyHint.text(3));
        this.multEight = new ModifyIconButton(b -> menu.modifyPattern(8),
                ModifyIcon.MULTIPLY_8,
                ButtonToolTips.PatternMultiply.text(8),
                ButtonToolTips.PatternMultiplyHint.text(8));
        this.divTwo = new ModifyIconButton(b -> menu.modifyPattern(-2),
                ModifyIcon.DIVISION_2,
                ButtonToolTips.PatternDivide.text(2),
                ButtonToolTips.PatternDivideHint.text(2));
        this.divThree = new ModifyIconButton(b -> menu.modifyPattern(-3),
                ModifyIcon.DIVISION_3,
                ButtonToolTips.PatternDivide.text(3),
                ButtonToolTips.PatternDivideHint.text(3));
        this.divEight = new ModifyIconButton(b -> menu.modifyPattern(-8),
                ModifyIcon.DIVISION_8,
                ButtonToolTips.PatternDivide.text(8),
                ButtonToolTips.PatternDivideHint.text(8));
        widgets.add("mult2", this.multTwo);
        widgets.add("mult3", this.multThree);
        widgets.add("mult8", this.multEight);
        widgets.add("div2", this.divTwo);
        widgets.add("div3", this.divThree);
        widgets.add("div8", this.divEight);
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
        BG.dest(bounds.getX() + 9, bounds.getY() + bounds.getHeight() - 164).blit(guiGraphics);
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
    public ItemStack getTabIconItem() {
        return Items.FURNACE.getDefaultInstance();
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
        this.multTwo.setVisibility(visible);
        this.multThree.setVisibility(visible);
        this.multEight.setVisibility(visible);
        this.divTwo.setVisibility(visible);
        this.divThree.setVisibility(visible);
        this.divEight.setVisibility(visible);

        screen.setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, !visible);
        screen.setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, !visible);

        updateTooltipVisibility();
    }
}
