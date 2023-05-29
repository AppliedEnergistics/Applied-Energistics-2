package appeng.client.guidebook.screen;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import appeng.client.gui.style.Blitter;
import appeng.client.guidebook.GuidebookText;
import appeng.client.guidebook.color.LightDarkMode;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.core.AppEng;

/**
 * Button found in the toolbar at the top of {@link GuideScreen}.
 */
class TopNavButton extends Button {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;

    private final Role role;

    public TopNavButton(int x, int y, Role role, Runnable callback) {
        super(
                x,
                y,
                WIDTH,
                HEIGHT,
                role.actionText,
                btn -> callback.run(),
                Supplier::get);
        this.role = role;
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

        var resolved = color.resolve(LightDarkMode.current());

        Blitter.texture(AppEng.makeId("textures/guide/topnav_buttons.png"), 64, 16)
                .src(role.iconSrcX, 0, 16, 16)
                .dest(getX(), getY(), 16, 16)
                .colorArgb(resolved)
                .blit(poseStack);
    }

    enum Role {
        BACK(GuidebookText.GuidebookHistoryGoBack.text(), 0),
        FORWARD(GuidebookText.GuidebookHistoryGoForward.text(), 16),
        CLOSE(GuidebookText.GuidebookClose.text(), 32);

        final Component actionText;

        final int iconSrcX;

        Role(Component actionText, int iconSrcX) {
            this.actionText = actionText;
            this.iconSrcX = iconSrcX;
        }
    }
}
