package appeng.client.gui.implementations;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

import appeng.api.client.AEKeyRendering;
import appeng.api.config.LockCraftingMode;
import appeng.api.stacks.AmountFormat;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;

public class PatternProviderLockReason implements ICompositeWidget {
    protected boolean visible = false;
    protected int x;
    protected int y;

    private final PatternProviderScreen<?> screen;

    public PatternProviderLockReason(PatternProviderScreen<?> screen) {
        this.screen = screen;
    }

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, 126, 16);
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        var menu = screen.getMenu();

        Icon icon;
        Component lockStatusText;
        if (menu.getCraftingLockedReason() == LockCraftingMode.NONE) {
            icon = Icon.UNLOCKED;
            lockStatusText = GuiText.CraftingLockIsUnlocked.text()
                    .setStyle(Style.EMPTY.withColor(Mth.color(125 / 255f, 169 / 255f, 210 / 255f)));
        } else {
            icon = Icon.LOCKED;
            lockStatusText = GuiText.CraftingLockIsLocked.text()
                    .setStyle(Style.EMPTY.withColor(Mth.color(193 / 255f, 66 / 255f, 75 / 255f)));
        }

        icon.getBlitter().dest(x, y).blit(guiGraphics);
        guiGraphics.drawString(Minecraft.getInstance().font, lockStatusText, x + 15, y + 5, -1, false);
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        var menu = screen.getMenu();
        var tooltip = switch (menu.getCraftingLockedReason()) {
            case NONE -> null;
            case LOCK_UNTIL_PULSE -> InGameTooltip.CraftingLockedUntilPulse.text();
            case LOCK_WHILE_HIGH -> InGameTooltip.CraftingLockedByRedstoneSignal.text();
            case LOCK_WHILE_LOW -> InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
            case LOCK_UNTIL_RESULT -> {
                var stack = menu.getUnlockStack();
                Component stackName;
                Component stackAmount;
                if (stack != null) {
                    stackName = AEKeyRendering.getDisplayName(stack.what());
                    stackAmount = Component.literal(stack.what().formatAmount(stack.amount(), AmountFormat.FULL));
                } else {
                    stackName = Component.literal("ERROR");
                    stackAmount = Component.literal("ERROR");

                }
                yield InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount);
            }
        };

        return tooltip != null ? new Tooltip(tooltip) : null;
    }
}
