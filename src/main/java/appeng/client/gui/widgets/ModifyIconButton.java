package appeng.client.gui.widgets;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;

public final class ModifyIconButton extends Button implements ITooltip {

    private final ModifyIcon icon;

    private final Component displayName;
    private final Component displayValue;

    public ModifyIconButton(Button.OnPress onPress, ModifyIcon icon, Component displayName, Component displayValue) {
        super(0, 0, 8, 8, Component.empty(), onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.displayName = displayName;
        this.displayValue = displayValue;
    }

    public void setVisibility(boolean vis) {
        visible = vis;
        active = vis;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (visible) {
            Blitter blitter = icon.getBlitter();
            if (!active) {
                blitter.opacity(0.5F);
            }

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            if (isFocused()) {
                guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), -1);
                guiGraphics.fill(getX() - 1, getY(), getX(), getY() + height, -1);
                guiGraphics.fill(getX() + width, getY(), getX() + width + 1, getY() + height, -1);
                guiGraphics.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, -1);
            }

            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(getX(), getY(), 0.0F);
            pose.scale(0.5F, 0.5F, 1.0F);
            Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(0, 0).blit(guiGraphics);
            blitter.dest(0, 0).blit(guiGraphics);
            pose.popPose();
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), 8, 8);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(Component.empty().append(displayName).append("\n").append(displayValue));
    }
}
