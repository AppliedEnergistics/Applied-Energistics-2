package appeng.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import appeng.core.AppEng;

public class AE2Button extends Button {
    protected static final WidgetSprites SPRITES = new WidgetSprites(
            AppEng.makeId("button"), AppEng.makeId("button_disabled"), AppEng.makeId("button_highlighted"));

    public AE2Button(int pX, int pY, int pWidth, int pHeight, Component component, OnPress onPress) {
        super(pX, pY, pWidth, pHeight, component, onPress, supplier -> Component.empty());
    }

    public AE2Button(Component component, OnPress onPress) {
        super(0, 0, 0, 0, component, onPress, supplier -> Component.empty());
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        pGuiGraphics.blitSprite(
                RenderType::guiTextured,
                SPRITES.get(this.active, this.isHovered()),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(alpha));
        if (!this.active) {
            this.renderButtonText(pGuiGraphics, minecraft.font, 2, 0x413f54 | Mth.ceil(this.alpha * 255.0F) << 24, -1);
        } else if (this.isHovered()) {
            this.renderButtonText(pGuiGraphics, minecraft.font, 2, 0x517497 | Mth.ceil(this.alpha * 255.0F) << 24, 0);
        } else {
            this.renderButtonText(pGuiGraphics, minecraft.font, 2, 0xf2f2f2 | Mth.ceil(this.alpha * 255.0F) << 24, 1);
        }
    }

    public static void renderButtonText(GuiGraphics pGuiGraphics, Font pFont, Component pText, int pMinX, int pMinY,
            int pMaxX, int pMaxY, int yOffset, int pColor) {
        renderButtonText(pGuiGraphics, pFont, pText, (pMinX + pMaxX) / 2, pMinX, pMinY, pMaxX, pMaxY, yOffset, pColor);
    }

    public static void renderButtonText(GuiGraphics pGuiGraphics, Font pFont, Component pText, int pCenterX, int pMinX,
            int pMinY, int pMaxX, int pMaxY, int yOffset, int pColor) {
        int i = pFont.width(pText);
        int j = (pMinY + pMaxY - 9) / 2 + 1;
        int k = pMaxX - pMinX;
        if (i > k) {
            int l = i - k;
            double d0 = (double) Util.getMillis() / 1000.0;
            double d1 = Math.max((double) l * 0.5, 3.0);
            double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
            double d3 = Mth.lerp(d2, 0.0, (double) l);
            pGuiGraphics.enableScissor(pMinX, pMinY, pMaxX, pMaxY);
            pGuiGraphics.drawString(pFont, pText, pMinX - (int) d3, j, pColor, false);
            pGuiGraphics.disableScissor();
        } else {
            int i1 = Mth.clamp(pCenterX, pMinX + i / 2, pMaxX - i / 2);
            FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
            pGuiGraphics.drawString(pFont, formattedcharsequence, i1 - pFont.width(formattedcharsequence) / 2,
                    j - yOffset, pColor, false);
        }
    }

    protected void renderButtonText(GuiGraphics pGuiGraphics, Font pFont, int pWidth, int pColor, int yOffset) {
        int i = this.getX() + pWidth;
        int j = this.getX() + this.getWidth() - pWidth;
        renderButtonText(pGuiGraphics, pFont, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(),
                yOffset, pColor);
    }
}
