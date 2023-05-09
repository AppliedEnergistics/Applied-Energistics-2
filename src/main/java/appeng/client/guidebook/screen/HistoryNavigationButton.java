package appeng.client.guidebook.screen;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import appeng.client.gui.style.Blitter;
import appeng.client.guidebook.GuidebookText;
import appeng.client.guidebook.render.LightDarkMode;
import appeng.client.guidebook.render.SymbolicColor;
import appeng.core.AppEng;

public class HistoryNavigationButton extends Button {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;

    private final Direction direction;

    public HistoryNavigationButton(int x, int y, Direction direction, Runnable callback) {
        super(
                x,
                y,
                WIDTH,
                HEIGHT,
                direction.actionText,
                btn -> callback.run(),
                Supplier::get);
        this.direction = direction;
        setTooltip(Tooltip.create(getMessage()));
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        var color = SymbolicColor.ICON_BUTTON_NORMAL;

        if (!isActive()) {
            color = SymbolicColor.ICON_BUTTON_DISABLED;
        } else if (isHovered()) {
            color = SymbolicColor.ICON_BUTTON_HOVER;
        }

        var resolved = color.resolve(LightDarkMode.LIGHT_MODE);

        Blitter.texture(AppEng.makeId("textures/guide/arrow.png"), 32, 16)
                .src(direction.iconSrcX, 0, 16, 16)
                .dest(getX(), getY(), 16, 16)
                .colorArgb(resolved)
                .blit(poseStack);
    }

    enum Direction {
        BACK(GuidebookText.GuidebookHistoryGoBack.text(), 0),
        FORWARD(GuidebookText.GuidebookHistoryGoForward.text(), 16);

        final Component actionText;

        final int iconSrcX;

        Direction(Component actionText, int iconSrcX) {
            this.actionText = actionText;
            this.iconSrcX = iconSrcX;
        }
    }
}
