package appeng.client.gui.implementations;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import appeng.api.client.AEStackRendering;
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

    private final PatternProviderScreen screen;

    public PatternProviderLockReason(PatternProviderScreen screen) {
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
    public void drawForegroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        var menu = screen.getMenu();

        Icon icon;
        Component lockStatusText;
        if (menu.getCraftingLockedReason() == LockCraftingMode.NONE) {
            icon = Icon.UNLOCKED;
            lockStatusText = GuiText.CraftingLockIsUnlocked.text()
                    .withStyle(ChatFormatting.DARK_GREEN);
        } else {
            icon = Icon.LOCKED;
            lockStatusText = GuiText.CraftingLockIsLocked.text()
                    .withStyle(ChatFormatting.DARK_RED);
        }

        icon.getBlitter().dest(x, y).blit(poseStack, zIndex);
        Minecraft.getInstance().font.draw(poseStack, lockStatusText, x + 15, y + 5, -1);
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
                    stackName = AEStackRendering.getDisplayName(stack.what());
                    stackAmount = new TextComponent(stack.what().formatAmount(stack.amount(), AmountFormat.FULL));
                } else {
                    stackName = new TextComponent("ERROR");
                    stackAmount = new TextComponent("ERROR");

                }
                yield InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount);
            }
        };

        return tooltip != null ? new Tooltip(tooltip) : null;
    }
}
