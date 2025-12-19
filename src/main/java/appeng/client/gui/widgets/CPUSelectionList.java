package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import appeng.api.stacks.AmountFormat;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.Color;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.menu.me.crafting.CraftingStatusMenu;

public class CPUSelectionList implements ICompositeWidget {

    private static final int ROWS = 6;

    private final Blitter background;
    private final Blitter buttonBg;
    private final Blitter buttonBgSelected;
    private final CraftingStatusMenu menu;
    private final Color textColor;
    private final int selectedColor;
    private final Scrollbar scrollbar;

    // Relative to the origin of the current screen (not window)
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public CPUSelectionList(CraftingStatusMenu menu, Scrollbar scrollbar, ScreenStyle style) {
        this.menu = menu;
        this.scrollbar = scrollbar;
        this.background = style.getImage("cpuList");
        this.buttonBg = style.getImage("cpuListButton");
        this.buttonBgSelected = style.getImage("cpuListButtonSelected");
        this.textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR);
        this.selectedColor = style.getColor(PaletteColor.SELECTION_COLOR).toARGB();
        this.scrollbar.setCaptureMouseWheel(false);
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
    public boolean onMouseWheel(Point mousePos, double delta) {
        scrollbar.onMouseWheel(mousePos, delta);
        return true;
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        var cpu = hitTestCpu(new Point(mouseX, mouseY));
        if (cpu != null) {
            var tooltipLines = new ArrayList<Component>();
            tooltipLines.add(getCpuName(cpu));

            // Show the number of coprocessors if any are installed
            var coProcessors = cpu.coProcessors();
            if (coProcessors == 1) {
                tooltipLines.add(ButtonToolTips.CpuStatusCoProcessor.text(Tooltips.ofNumber(coProcessors))
                        .withStyle(ChatFormatting.GRAY));
            } else if (coProcessors > 1) {
                tooltipLines.add(ButtonToolTips.CpuStatusCoProcessors.text(Tooltips.ofNumber(coProcessors))
                        .withStyle(ChatFormatting.GRAY));
            }

            // Show the amount of storage in the Crafting CPU
            tooltipLines.add(ButtonToolTips.CpuStatusStorage.text(Tooltips.ofBytes(cpu.storage()))
                    // Vanilla text formatting is broken and inherits the color of the 1st placeholder in the text
                    .withStyle(ChatFormatting.GRAY));

            // Show if the CPU is player or automation only
            var modeText = switch (cpu.mode()) {
                case PLAYER_ONLY -> ButtonToolTips.CpuSelectionModePlayersOnly.text();
                case MACHINE_ONLY -> ButtonToolTips.CpuSelectionModeAutomationOnly.text();
                default -> null;
            };
            if (modeText != null) {
                tooltipLines.add(modeText);
            }

            // Show info on the currently executing job
            var currentJob = cpu.currentJob();
            if (currentJob != null) {
                tooltipLines.add(
                        ButtonToolTips.CpuStatusCrafting.text(
                                Tooltips.ofAmount(currentJob)).append(" ").append(currentJob.what().getDisplayName()));
                tooltipLines.add(
                        ButtonToolTips.CpuStatusCraftedIn.text(
                                Tooltips.ofPercent(cpu.progress()),
                                Tooltips.ofDuration(cpu.elapsedTimeNanos(), TimeUnit.NANOSECONDS)));
            }
            if (cpu.isSuspended()) {
                tooltipLines.add(ButtonToolTips.CpuStatusSuspended.text().withStyle(ChatFormatting.YELLOW));
            }
            return new Tooltip(tooltipLines);
        }
        return null;
    }

    @Override
    public boolean onMouseUp(Point mousePos, int button) {
        var cpu = hitTestCpu(mousePos);
        if (cpu != null) {
            menu.selectCpu(cpu.serial());
            return true;
        }

        return false;
    }

    @Nullable
    private CraftingStatusMenu.CraftingCpuListEntry hitTestCpu(Point mousePos) {
        var relX = mousePos.getX() - bounds.getX();
        var relY = mousePos.getY() - bounds.getY();
        relX -= 8;
        if (relX < 0 || relX >= buttonBg.getSrcWidth()) {
            return null;
        }

        relY -= 19;
        var buttonIdx = scrollbar.getCurrentScroll() + relY / (buttonBg.getSrcHeight() + 1);
        if (relY % (buttonBg.getSrcHeight() + 1) == buttonBg.getSrcHeight()) {
            // Clicked right between two buttons
            return null;
        }
        if (relY < 0 || buttonIdx >= menu.cpuList.cpus().size()) {
            // Clicked above first or below last button
            return null;
        }

        var cpus = menu.cpuList.cpus();
        if (buttonIdx >= 0 && buttonIdx < cpus.size()) {
            return cpus.get(buttonIdx);
        }

        return null;
    }

    @Override
    public void updateBeforeRender() {
        var hiddenRows = Math.max(0, menu.cpuList.cpus().size() - ROWS);
        scrollbar.setRange(0, hiddenRows, ROWS / 3);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        var x = bounds.getX() + this.bounds.getX();
        var y = bounds.getY() + this.bounds.getY();
        background.dest(
                x,
                y,
                this.bounds.getWidth(),
                this.bounds.getHeight()).blit(guiGraphics);

        // Move to first button
        x += 8;
        y += 19;

        var pose = guiGraphics.pose();

        var font = Minecraft.getInstance().font;
        var cpus = menu.cpuList.cpus().subList(
                Mth.clamp(scrollbar.getCurrentScroll(), 0, menu.cpuList.cpus().size()),
                Mth.clamp(scrollbar.getCurrentScroll() + ROWS, 0, menu.cpuList.cpus().size()));
        for (var cpu : cpus) {
            if (cpu.serial() == menu.getSelectedCpuSerial()) {
                buttonBgSelected.dest(x, y).blit(guiGraphics);
            } else {
                buttonBg.dest(x, y).blit(guiGraphics);
            }

            var name = getCpuName(cpu);
            pose.pushPose();
            pose.translate(x + 3, y + 2, 0);
            pose.scale(0.666f, 0.666f, 1);
            guiGraphics.drawString(font, name, 0, 0, textColor.toARGB(), false);
            pose.popPose();

            var infoBar = new InfoBar();

            var currentJob = cpu.currentJob();
            if (currentJob != null) {
                // Show what was initially requested
                infoBar.add(Icon.S_CRAFT, 1f, x + 2, y + 9);
                var craftAmt = currentJob.what().formatAmount(currentJob.amount(), AmountFormat.SLOT);
                infoBar.add(craftAmt, textColor.toARGB(), 0.666f, x + 14, y + 13);
                infoBar.add(currentJob.what(), 0.666f, x + 55, y + 9);
                if (cpu.isSuspended())
                    infoBar.add(Icon.S_SUSPENDED, 0.7f, x + 58, y + 1);

                // Draw a bar at the bottom of the button to indicate job progress
                var progress = (int) (cpu.progress() * (buttonBg.getSrcWidth() - 1));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(1, -1, 0);
                guiGraphics.fill(
                        x,
                        y + buttonBg.getSrcHeight() - 2,
                        x + progress,
                        y + buttonBg.getSrcHeight() - 1,
                        menu.getSelectedCpuSerial() == cpu.serial() ? 0xFF7da9d2 : (selectedColor));
                guiGraphics.pose().popPose();

            } else {
                infoBar.add(Icon.S_STORAGE, 1f, x + 27, y + 9);

                String storageAmount = formatStorage(cpu);
                infoBar.add(storageAmount, textColor.toARGB(), 0.666f, x + 39, y + 13);

                if (cpu.coProcessors() > 0) {
                    infoBar.add(Icon.S_PROCESSOR, 1f, x + 2, y + 9);
                    String coProcessorCount = String.valueOf(cpu.coProcessors());
                    infoBar.add(coProcessorCount, textColor.toARGB(), 0.666f, x + 14, y + 13);
                }

                switch (cpu.mode()) {
                    case PLAYER_ONLY -> infoBar.add(Icon.S_TERMINAL, 1f, x + 55, y + 9);
                    case MACHINE_ONLY -> infoBar.add(Icon.S_MACHINE, 1f, x + 55, y + 9);
                }
            }

            infoBar.render(guiGraphics, x + 2, y + buttonBg.getSrcHeight() - 12);

            y += buttonBg.getSrcHeight() + 1;
        }
    }

    private String formatStorage(CraftingStatusMenu.CraftingCpuListEntry cpu) {
        long storage = cpu.storage();

        if (storage >= 1024 * 1024) {
            return (storage / (1024 * 1024)) + "M";
        } else {
            return (storage / 1024) + "k";
        }
    }

    private Component getCpuName(CraftingStatusMenu.CraftingCpuListEntry cpu) {
        return cpu.name() != null ? cpu.name() : GuiText.CPUs.text().append(String.format(" #%d", cpu.serial()));
    }
}
